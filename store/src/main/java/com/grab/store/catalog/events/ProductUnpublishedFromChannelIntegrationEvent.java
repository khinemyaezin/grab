package com.grab.store.catalog.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductUnpublishedFromChannelIntegrationEvent(
        String variantId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
