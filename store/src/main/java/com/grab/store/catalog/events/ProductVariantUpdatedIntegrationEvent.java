package com.grab.store.catalog.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantUpdatedIntegrationEvent(
        String productId,
        String variantId,
        String sku,
        Instant occurredAt,
        int version
) implements Event {
}
