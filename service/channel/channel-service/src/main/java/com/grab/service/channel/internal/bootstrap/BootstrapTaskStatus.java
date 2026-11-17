package com.grab.service.channel.internal.bootstrap;

import java.time.Duration;

/**
 * Snapshot of one bootstrap task's progress, exposed through the {@code bootstrap} health indicator.
 */
public record BootstrapTaskStatus(String key, boolean required, State state, int attempts, Duration duration,
                                  String lastError) {

    public enum State {
        PENDING, RUNNING, SATISFIED, FAILED
    }

    static BootstrapTaskStatus pending(String key, boolean required) {
        return new BootstrapTaskStatus(key, required, State.PENDING, 0, Duration.ZERO, null);
    }

    BootstrapTaskStatus running(int attempts, Duration duration, String lastError) {
        return new BootstrapTaskStatus(key, required, State.RUNNING, attempts, duration, lastError);
    }

    BootstrapTaskStatus satisfied(int attempts, Duration duration) {
        return new BootstrapTaskStatus(key, required, State.SATISFIED, attempts, duration, null);
    }

    BootstrapTaskStatus failed(int attempts, Duration duration, String lastError) {
        return new BootstrapTaskStatus(key, required, State.FAILED, attempts, duration, lastError);
    }

    public boolean blocksReadiness() {
        return required && state != State.SATISFIED;
    }
}
