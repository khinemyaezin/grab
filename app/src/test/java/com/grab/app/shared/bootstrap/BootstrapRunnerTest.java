package com.grab.app.shared.bootstrap;

import com.grab.framework.bootstrap.BootstrapTask;
import com.grab.framework.bootstrap.BootstrapTaskException;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapRunnerTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-14T10:00:00Z"));
    private final List<Long> sleeps = new ArrayList<>();
    private final BootstrapRunner.Sleeper sleeper = millis -> {
        sleeps.add(millis);
        clock.advance(Duration.ofMillis(millis));
    };
    private final BootstrapProperties properties =
            new BootstrapProperties(true, 3, Duration.ofSeconds(1), Duration.ofSeconds(4), Duration.ofSeconds(2));

    @Test
    void successfulTask_isSatisfiedAndUnblocksReadiness() {
        FakeTask task = new FakeTask("a", true, SingletonJobExecutor.local());

        BootstrapRunner runner = runner(task);
        assertTrue(runner.isReadinessBlocked());
        runner.runAll();

        assertEquals(1, task.executions.get());
        assertFalse(runner.isReadinessBlocked());
        assertTrue(runner.isFinished());
        assertEquals(BootstrapTaskStatus.State.SATISFIED, status(runner, "a").state());
    }

    @Test
    void transientFailure_isRetriedWithExponentialBackoff() {
        FakeTask task = new FakeTask("a", true, SingletonJobExecutor.local());
        task.failuresBeforeSuccess = 2;

        BootstrapRunner runner = runner(task);
        runner.runAll();

        assertEquals(3, task.executions.get());
        assertEquals(List.of(1000L, 2000L), sleeps);
        assertEquals(BootstrapTaskStatus.State.SATISFIED, status(runner, "a").state());
    }

    @Test
    void exhaustedRetries_failRequiredTaskAndKeepReadinessBlocked() {
        FakeTask task = new FakeTask("a", true, SingletonJobExecutor.local());
        task.failuresBeforeSuccess = Integer.MAX_VALUE;

        BootstrapRunner runner = runner(task);
        runner.runAll();

        assertEquals(3, task.executions.get());
        assertEquals(BootstrapTaskStatus.State.FAILED, status(runner, "a").state());
        assertTrue(runner.isReadinessBlocked());
    }

    @Test
    void permanentFailure_isNotRetried() {
        FakeTask task = new FakeTask("a", true, SingletonJobExecutor.local());
        task.permanent = true;

        BootstrapRunner runner = runner(task);
        runner.runAll();

        assertEquals(1, task.executions.get());
        assertTrue(sleeps.isEmpty());
        assertTrue(status(runner, "a").lastError().contains("CONFLICT"));
        assertTrue(runner.isReadinessBlocked());
    }

    @Test
    void optionalTaskFailure_doesNotBlockReadiness_andLaterTasksStillRun() {
        FakeTask optional = new FakeTask("optional", false, SingletonJobExecutor.local());
        optional.permanent = true;
        FakeTask required = new FakeTask("required", true, SingletonJobExecutor.local());

        BootstrapRunner runner = runner(optional, required);
        runner.runAll();

        assertEquals(1, required.executions.get());
        assertFalse(runner.isReadinessBlocked());
    }

    @Test
    void lockHeldElsewhere_waitsThenExecutesOnceReleased() {
        AtomicInteger lockAttempts = new AtomicInteger();
        List<String> lockNames = new ArrayList<>();
        SingletonJobExecutor contended = (SingletonJob job, Runnable work) -> {
            lockNames.add(job.name());
            if (lockAttempts.incrementAndGet() <= 2) {
                return false;
            }
            work.run();
            return true;
        };
        FakeTask task = new FakeTask("saleschannel.ensure-marketplace", true, contended);

        BootstrapRunner runner = runner(task);
        runner.runAll();

        assertEquals(1, task.executions.get());
        assertEquals(List.of(2000L, 2000L), sleeps);
        assertEquals("bootstrap:saleschannel.ensure-marketplace", lockNames.get(0));
        assertEquals(1, status(runner, task.key()).attempts());
    }

    @Test
    void lockNeverReleased_timesOut() {
        SingletonJobExecutor neverAcquired = (job, work) -> false;
        FakeTask task = new FakeTask("a", true, neverAcquired);
        task.timeout = Duration.ofSeconds(5);

        BootstrapRunner runner = runner(task);
        runner.runAll();

        assertEquals(0, task.executions.get());
        assertEquals(BootstrapTaskStatus.State.FAILED, status(runner, "a").state());
    }

    @Test
    void satisfiedTask_skipsExecution() {
        FakeTask task = new FakeTask("a", true, SingletonJobExecutor.local());
        task.satisfied = true;

        BootstrapRunner runner = runner(task);
        runner.runAll();

        assertEquals(0, task.executions.get());
        assertFalse(runner.isReadinessBlocked());
    }

    private BootstrapRunner runner(BootstrapTask... tasks) {
        return new BootstrapRunner(List.of(tasks), properties, clock, sleeper);
    }

    private static BootstrapTaskStatus status(BootstrapRunner runner, String key) {
        return runner.statuses().stream().filter(s -> s.key().equals(key)).findFirst().orElseThrow();
    }

    private static final class FakeTask implements BootstrapTask {
        private final String key;
        private final boolean required;
        private final SingletonJobExecutor executor;
        private final AtomicInteger executions = new AtomicInteger();
        private int failuresBeforeSuccess;
        private boolean permanent;
        private boolean satisfied;
        private Duration timeout = Duration.ofMinutes(2);

        private FakeTask(String key, boolean required, SingletonJobExecutor executor) {
            this.key = key;
            this.required = required;
            this.executor = executor;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public boolean required() {
            return required;
        }

        @Override
        public Duration timeout() {
            return timeout;
        }

        @Override
        public void execute() {
            int attempt = executions.incrementAndGet();
            if (permanent) {
                throw BootstrapTaskException.permanent("Publication CONFLICT");
            }
            if (attempt <= failuresBeforeSuccess) {
                throw new IllegalStateException("database not ready");
            }
        }

        @Override
        public boolean isSatisfied() {
            return satisfied;
        }

        @Override
        public SingletonJobExecutor singletonJobExecutor() {
            return executor;
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
