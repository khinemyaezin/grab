package com.grab.framework.scheduling;

import java.time.Duration;
import java.util.Objects;

/**
 * A scheduled job that must run on at most one replica at a time (scheduling class B).
 *
 * @param name           cluster-wide lock name, e.g. {@code catalog.outbox.cleanup}
 * @param lockAtMostFor  safety release if the holder dies (~3x expected run time)
 * @param lockAtLeastFor minimum hold time, preventing back-to-back runs on other nodes (~50-80% of interval)
 */
public record SingletonJob(String name, Duration lockAtMostFor, Duration lockAtLeastFor) {

    public SingletonJob {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(lockAtMostFor, "lockAtMostFor");
        lockAtLeastFor = lockAtLeastFor == null ? Duration.ZERO : lockAtLeastFor;
        if (lockAtLeastFor.compareTo(lockAtMostFor) > 0) {
            throw new IllegalArgumentException("lockAtLeastFor must not exceed lockAtMostFor for job " + name);
        }
    }
}
