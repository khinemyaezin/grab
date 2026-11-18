package com.grab.service.contract.event.merchandising.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantUpdatedIntegrationEvent(
        String productId,
        String variantId,
        String sku,
        boolean manageInventory,
        Instant occurredAt,
        int version
) implements Event {
}
