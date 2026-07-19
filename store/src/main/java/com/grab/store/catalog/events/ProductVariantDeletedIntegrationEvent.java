package com.grab.store.catalog.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantDeletedIntegrationEvent(
        String productId,
        String variantId,
        Instant occurredAt,
        int version
) implements Event {
}
