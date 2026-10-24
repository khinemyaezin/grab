package com.grab.store.shared.events.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantRestoredIntegrationEvent(
        String productId,
        String variantId,
        Instant occurredAt,
        int version
) implements Event {
}
