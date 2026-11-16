package com.grab.outbox.infrastructure.adapter.in.relay;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxWorkSource;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;

import java.time.Duration;

public final class OutboxRelays {

    private OutboxRelays() {
    }

    public static <ID> void bind(OutboxRelay<ID> relay, DeliverOutboxEventsUseCase<ID> delivery) {
        if (!relay.isActive()) {
            return;
        }
        relay.setHandler(work -> {
            if (work.source() == OutboxWorkSource.COLD) {
                ClaimedOutboxEvent<ID> claim = new ClaimedOutboxEvent<>(work.id(), work.claimToken());
                delivery.deliverClaimed(claim);
            } else {
                delivery.deliverHot(work.id());
            }
        });
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
