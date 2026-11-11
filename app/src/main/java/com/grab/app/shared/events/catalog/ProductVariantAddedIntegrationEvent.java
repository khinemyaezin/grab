package com.grab.app.shared.events.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantAddedIntegrationEvent(
        String productId,
        String variantId,
        String sku,
        String productName,
        boolean manageInventory,
        Instant occurredAt,
        int version
) implements Event {
}
