package com.grab.app.shared.bootstrap;

import com.grab.framework.bootstrap.BootstrapTask;
import com.grab.framework.bootstrap.BootstrapTaskException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs every {@link BootstrapTask} once per process start, in {@code @Order}, on a dedicated thread after
 * {@link ApplicationReadyEvent}. Each task runs under the cluster lock {@code bootstrap:<key>}; a replica that loses
 * the lock waits for it and then executes the (idempotent) task itself. Transient failures are retried with
 * exponential backoff within the task timeout; permanent failures stop immediately.
 * Required tasks that are not satisfied keep {@link BootstrapHealthIndicator} DOWN, which is part of the
 * readiness group, so Kubernetes holds traffic instead of crash-looping.
 */
@Component
public class BootstrapRunner implements DisposableBean {

    private static final Logger log = Loggers.getLogger(BootstrapRunner.class);
    private static final String LOCK_PREFIX = "bootstrap:";

    private final List<BootstrapTask> tasks;
    private final BootstrapProperties properties;
    private final Clock clock;
    private final Sleeper sleeper;
    private final Map<String, BootstrapTaskStatus> statuses = new ConcurrentHashMap<>();
    private volatile Thread worker;
    private volatile boolean finished;

    @Autowired
    public BootstrapRunner(ObjectProvider<BootstrapTask> tasks, BootstrapProperties properties) {
        this(tasks.orderedStream().toList(), properties, Clock.systemUTC(), Thread::sleep);
    }

    BootstrapRunner(List<BootstrapTask> tasks, BootstrapProperties properties, Clock clock, Sleeper sleeper) {
        this.tasks = List.copyOf(tasks);
        this.properties = properties;
        this.clock = clock;
        this.sleeper = sleeper;
        for (BootstrapTask task : this.tasks) {
            BootstrapTaskStatus pending = BootstrapTaskStatus.pending(task.key(), task.required());
            statuses.put(task.key(), pending);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Thread thread = new Thread(this::runAll, "bootstrap-runner");
        thread.setDaemon(true);
        worker = thread;
        thread.start();
    }

    /**
     * Runs all tasks sequentially on the calling thread.
     */
    public void runAll() {
        log.info("Bootstrap starting tasks: {}", tasks.stream().map(BootstrapTask::key).toList());
        for (BootstrapTask task : tasks) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            run(task);
        }
        finished = true;
        log.info("Bootstrap finished readinessBlocked: {}", isReadinessBlocked());
    }

    public Collection<BootstrapTaskStatus> statuses() {
        Map<String, BootstrapTaskStatus> ordered = new LinkedHashMap<>();
        for (BootstrapTask task : tasks) {
            ordered.put(task.key(), statuses.get(task.key()));
        }
        return ordered.values();
    }

    public boolean isReadinessBlocked() {
        return statuses.values().stream().anyMatch(BootstrapTaskStatus::blocksReadiness);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean gatesReadiness() {
        return properties.gateReadiness();
    }

    @Override
    public void destroy() {
        Thread thread = worker;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void run(BootstrapTask task) {
        String key = task.key();
        Instant start = clock.instant();
        Instant deadline = start.plus(task.timeout());
        SingletonJob lock = new SingletonJob(LOCK_PREFIX + key, task.timeout(), Duration.ZERO);
        SingletonJobExecutor executor = task.singletonJobExecutor();
        int attempts = 0;
        String lastError = null;
        while (true) {
            Duration elapsed = Duration.between(start, clock.instant());
            update(key, statuses.get(key).running(attempts, elapsed, lastError));
            try {
                if (task.isSatisfied()) {
                    succeed(key, attempts, start);
                    return;
                }
                attempts++;
                boolean ran = executor.execute(lock, task::execute);
                if (ran) {
                    succeed(key, attempts, start);
                    return;
                }
                attempts--;
                log.info("Bootstrap task: {} locked by another replica, waiting", key);
                if (!pause(properties.lockPollInterval(), deadline)) {
                    fail(task, attempts, start, "timed out waiting for the cluster lock");
                    return;
                }
            } catch (BootstrapTaskException e) {
                lastError = e.getMessage();
                if (e.isPermanent()) {
                    fail(task, attempts, start, lastError);
                    return;
                }
                if (!retry(task, attempts, start, deadline, e)) {
                    return;
                }
            } catch (RuntimeException e) {
                lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                if (!retry(task, attempts, start, deadline, e)) {
                    return;
                }
            }
        }
    }

    private boolean retry(BootstrapTask task, int attempts, Instant start, Instant deadline, RuntimeException error) {
        String message = error.getClass().getSimpleName() + ": " + error.getMessage();
        if (attempts >= properties.maxAttempts()) {
            fail(task, attempts, start, message);
            return false;
        }
        Duration backoff = backoff(attempts);
        log.warn("Bootstrap task: {} attempt: {} failed, retrying in: {} error: {}",
                task.key(), attempts, backoff, message);
        boolean resumed = pause(backoff, deadline);
        if (!resumed) {
            fail(task, attempts, start, "timed out after: " + message);
        }
        return resumed;
    }

    private Duration backoff(int attempts) {
        long factor = 1L << Math.min(attempts - 1, 16);
        Duration delay = properties.initialBackoff().multipliedBy(factor);
        return delay.compareTo(properties.maxBackoff()) > 0 ? properties.maxBackoff() : delay;
    }

    /**
     * @return {@code false} when the deadline would be exceeded or the thread was interrupted
     */
    private boolean pause(Duration delay, Instant deadline) {
        Instant wakeUp = clock.instant().plus(delay);
        if (wakeUp.isAfter(deadline)) {
            return false;
        }
        try {
            sleeper.sleep(delay.toMillis());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void succeed(String key, int attempts, Instant start) {
        Duration duration = Duration.between(start, clock.instant());
        update(key, statuses.get(key).satisfied(attempts, duration));
        log.info("Bootstrap task: {} satisfied attempts: {} duration: {}", key, attempts, duration);
    }

    private void fail(BootstrapTask task, int attempts, Instant start, String error) {
        String key = task.key();
        Duration duration = Duration.between(start, clock.instant());
        update(key, statuses.get(key).failed(attempts, duration, error));
        if (task.required()) {
            log.error("Required bootstrap task: {} failed, readiness stays DOWN, attempts: {} error: {}",
                    key, attempts, error);
        } else {
            log.warn("Optional bootstrap task: {} failed attempts: {} error: {}", key, attempts, error);
        }
    }

    private void update(String key, BootstrapTaskStatus status) {
        statuses.put(key, status);
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
}
