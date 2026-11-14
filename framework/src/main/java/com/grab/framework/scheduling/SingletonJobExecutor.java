package com.grab.framework.scheduling;

/**
 * Runs a {@link SingletonJob} only if this replica wins the cluster-wide lock for it.
 * Implementations are backed by a lock table in the owning module's schema (ShedLock).
 */
@FunctionalInterface
public interface SingletonJobExecutor {

    /**
     * @return {@code true} when the task ran on this replica, {@code false} when another replica holds the lock
     */
    boolean execute(SingletonJob job, Runnable task);
}
