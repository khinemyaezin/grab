package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Singleton leases shared by jobs using this executor in a single replica. */
public final class LocalSingletonJobExecutor implements SingletonJobExecutor {

    private final Clock clock;
    private final ConcurrentMap<String, Lease> leases = new ConcurrentHashMap<>();

    public LocalSingletonJobExecutor() {
        this(Clock.systemUTC());
    }

    public LocalSingletonJobExecutor(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public boolean execute(SingletonJob job, Runnable task) {
        Instant now = clock.instant();
        Lease candidate = new Lease(now.plus(job.lockAtMostFor()));
        Lease acquired = leases.compute(job.name(), (name, current) -> {
            if (current != null && now.isBefore(current.expiresAt())) {
                return current;
            }
            return candidate;
        });
        if (acquired != candidate) {
            return false;
        }
        try {
            task.run();
            return true;
        } finally {
            Instant holdUntil = now.plus(job.lockAtLeastFor());
            leases.computeIfPresent(job.name(), (name, current) -> {
                if (current != candidate) {
                    return current;
                }
                return clock.instant().isBefore(holdUntil) ? new Lease(holdUntil) : null;
            });
        }
    }

    private record Lease(Instant expiresAt) {
    }
}
