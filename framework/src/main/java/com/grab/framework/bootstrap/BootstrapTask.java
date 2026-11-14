package com.grab.framework.bootstrap;

import com.grab.framework.scheduling.SingletonJobExecutor;

import java.time.Duration;

/**
 * Boot-time work that must run once per deploy, cluster-wide (scheduling class C), e.g. ensuring reference rows or
 * publishing a module's security manifest.
 * <p>
 * The runner executes {@link #execute()} under the cluster lock {@code bootstrap:<key>} obtained through
 * {@link #singletonJobExecutor()}. Replicas that lose the lock wait for it to be released and then execute again,
 * so {@link #execute()} MUST be idempotent (ensure pattern / monotonic writes). The lock is an optimisation; the
 * database constraint is the guarantee.
 */
public interface BootstrapTask {

    /**
     * Stable identifier, e.g. {@code saleschannel.ensure-marketplace}. Also used to name the cluster lock.
     */
    String key();

    /**
     * Whether readiness stays DOWN until this task succeeds.
     */
    boolean required();

    /**
     * Upper bound for the whole task, including retries and waiting for another replica.
     */
    Duration timeout();

    /**
     * Performs the work, typically by dispatching a command on the module's group bus.
     * Throw {@link BootstrapTaskException#permanent(String)} for failures that retrying cannot fix.
     */
    void execute();

    /**
     * Cheap read-only check that lets a replica skip {@link #execute()}. Defaults to {@code false} (always execute).
     */
    default boolean isSatisfied() {
        return false;
    }

    /**
     * Lock executor backed by the owning module's schema. Defaults to an unlocked executor.
     */
    default SingletonJobExecutor singletonJobExecutor() {
        return SingletonJobExecutor.local();
    }
}
