package com.grab.store.shared.events.inventory;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record LocationLinkedToChannelIntegrationEvent(
        String locationId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
