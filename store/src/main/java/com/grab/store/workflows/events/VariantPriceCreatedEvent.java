package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record VariantPriceCreatedEvent(
        String workflowId,
        String variantId,
        String sku,
        String priceSetId,
        Instant occurredAt,
        int version
) implements Event {
}
