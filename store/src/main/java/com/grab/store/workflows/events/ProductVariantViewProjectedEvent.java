package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductVariantViewProjectedEvent(
        String productId,
        String variantId,
        String sku,
        Instant occurredAt,
        int version
) implements Event {
}
