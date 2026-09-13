package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.OutboxRelay;

import java.time.Duration;

public final class OutboxRelays {

    private OutboxRelays() {
    }

    public static <ID> OutboxRelay<ID> moduleRelay(
            String name,
            boolean enabled,
            int workerCount,
            int queueCapacity,
            long drainTimeoutMs
    ) {
        if (!enabled) {
            return OutboxRelay.noop();
        }
        return new DualQueueOutboxRelay<>(
                name,
                workerCount,
                queueCapacity,
                queueCapacity,
                Duration.ofMillis(drainTimeoutMs)
        );
    }
}
