package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.contract.SingletonJobExecutorContract;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

class LocalSingletonJobExecutorTest extends SingletonJobExecutorContract {

    private final MutableClock clock = new MutableClock();
    private final SingletonJobExecutor executor = new LocalSingletonJobExecutor(clock);

    @Override
    protected SingletonJobExecutor owner() {
        return executor;
    }

    @Override
    protected SingletonJobExecutor competitor() {
        return executor;
    }

    @Override
    protected void awaitLease(Duration duration) {
        clock.now.updateAndGet(instant -> instant.plus(duration));
    }

    private static final class MutableClock extends Clock {

        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.EPOCH);

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return now.get();
        }
    }
}
