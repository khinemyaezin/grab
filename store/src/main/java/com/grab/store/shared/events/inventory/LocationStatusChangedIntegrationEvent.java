package com.grab.store.shared.events.inventory;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record LocationStatusChangedIntegrationEvent(
        String locationId,
        boolean active,
        Instant occurredAt,
        int version
) implements Event {
}
