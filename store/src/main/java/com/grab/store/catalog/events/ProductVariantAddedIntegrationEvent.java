package com.grab.store.catalog.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantAddedIntegrationEvent(
        String productId,
        String variantId,
        String sku,
        String productName,
        Instant occurredAt,
        int version
) implements Event {
}
