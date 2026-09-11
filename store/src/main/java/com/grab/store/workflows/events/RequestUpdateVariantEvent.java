package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestUpdateVariantEvent(
        String workflowId,
        String merchantId,
        String productId,
        String variantId,
        String sku,
        Instant occurredAt,
        int version
) implements Event {
}
