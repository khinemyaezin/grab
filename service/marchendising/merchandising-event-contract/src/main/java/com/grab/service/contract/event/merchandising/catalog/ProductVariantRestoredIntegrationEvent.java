package com.grab.service.contract.event.merchandising.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantRestoredIntegrationEvent(
        String productId,
        String variantId,
        Instant occurredAt,
        int version
) implements Event {
}
