package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record SellableProductProductCreatedEvent(
        String workflowId,
        String productId,
        List<String> skus,
        List<VariantRef> variants,
        Instant occurredAt,
        int version
) implements Event {

    public SellableProductProductCreatedEvent {
        skus = skus == null ? List.of() : List.copyOf(skus);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public record VariantRef(String variantId, String sku) {
    }
}
