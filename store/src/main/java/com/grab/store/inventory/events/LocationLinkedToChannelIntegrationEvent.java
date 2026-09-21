package com.grab.store.inventory.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record LocationLinkedToChannelIntegrationEvent(
        String locationId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
