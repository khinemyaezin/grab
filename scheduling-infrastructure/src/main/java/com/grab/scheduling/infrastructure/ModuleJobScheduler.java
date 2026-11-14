package com.grab.scheduling.infrastructure;

import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.PropertyResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ModuleJobScheduler implements SmartLifecycle {

    private final String module;
    private final List<ScheduledJob> jobs;
    private final int poolSize;
    private final List<ScheduledFuture<?>> scheduledRuns = new ArrayList<>();
    private volatile ThreadPoolTaskScheduler scheduler;

    public ModuleJobScheduler(String module, List<ScheduledJob> jobs, int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException(module + ".scheduling.pool-size must be positive");
        }
        this.module = module;
        this.jobs = List.copyOf(jobs);
        this.poolSize = poolSize;
        Set<String> names = new HashSet<>();
        for (ScheduledJob job : this.jobs) {
            if (!names.add(job.name())) {
                throw new IllegalArgumentException("Duplicate scheduled job " + job.name());
            }
            validateDelays(job);
        }
    }

    public static ModuleJobScheduler fromProperties(String module, List<ScheduledJob> jobs,
                                                    PropertyResolver properties) {
        int poolSize = properties.getProperty(module + ".scheduling.pool-size", Integer.class, 2);
        return new ModuleJobScheduler(module, jobs, poolSize);
    }

    public List<ScheduledJob> jobs() {
        return jobs;
    }

    @Override
    public int getPhase() {
        return DEFAULT_PHASE;
    }

    @Override
    public boolean isRunning() {
        return scheduler != null;
    }

    @Override
    public synchronized void start() {
        if (scheduler != null) {
            return;
        }
        ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
        pool.setPoolSize(poolSize);
        pool.setThreadNamePrefix(module + "-jobs-");
        pool.setRemoveOnCancelPolicy(true);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        scheduler = pool;
        try {
            for (ScheduledJob job : jobs) {
                Runnable body = switch (job) {
                    case CompetingWorkerTask worker -> worker.body();
                    case ClusterSingletonTask singleton -> () ->
                            singleton.executor().execute(singleton.job(), singleton.body());
                };
                Instant firstRun = pool.getClock().instant().plus(job.initialDelay());
                ScheduledFuture<?> run = pool.scheduleWithFixedDelay(body, firstRun, job.fixedDelay());
                scheduledRuns.add(run);
            }
        } catch (RuntimeException exception) {
            stop();
            throw exception;
        }
    }

    @Override
    public synchronized void stop() {
        if (scheduler == null) {
            return;
        }
        for (ScheduledFuture<?> run : scheduledRuns) {
            run.cancel(false);
        }
        scheduledRuns.clear();
        ScheduledThreadPoolExecutor executor = scheduler.getScheduledThreadPoolExecutor();
        scheduler.shutdown();
        awaitTermination(executor);
        scheduler = null;
    }

    private static void awaitTermination(ScheduledThreadPoolExecutor executor) {
        boolean interrupted = false;
        try {
            while (!executor.isTerminated()) {
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void validateDelays(ScheduledJob job) {
        Duration initial = job.initialDelay();
        Duration fixed = job.fixedDelay();
        if (initial.isNegative() || fixed.isNegative() || fixed.isZero()) {
            throw new IllegalArgumentException("Invalid scheduling delays for " + job.name());
        }
    }
}
