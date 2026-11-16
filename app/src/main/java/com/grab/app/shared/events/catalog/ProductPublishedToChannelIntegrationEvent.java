package com.grab.app.shared.events.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductPublishedToChannelIntegrationEvent(
        String variantId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
