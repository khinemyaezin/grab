package com.grab.store.catalog.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductStatusChangedIntegrationEvent(
        String productId,
        String oldStatus,
        String newStatus,
        Instant occurredAt,
        int version
) implements Event {
}
