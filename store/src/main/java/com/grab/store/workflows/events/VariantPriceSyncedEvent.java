package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record VariantPriceSyncedEvent(
        String workflowId,
        String variantId,
        String sku,
        String priceSetId,
        boolean created,
        Instant occurredAt,
        int version
) implements Event {
}
