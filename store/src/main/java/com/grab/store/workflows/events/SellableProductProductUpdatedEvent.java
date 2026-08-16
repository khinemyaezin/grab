package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record SellableProductProductUpdatedEvent(
        String workflowId,
        String productId,
        List<String> skus,
        List<VariantRef> variants,
        List<String> addedSkus,
        Instant occurredAt,
        int version
) implements Event {

    public SellableProductProductUpdatedEvent {
        skus = skus == null ? List.of() : List.copyOf(skus);
        variants = variants == null ? List.of() : List.copyOf(variants);
        addedSkus = addedSkus == null ? List.of() : List.copyOf(addedSkus);
    }

    public record VariantRef(String variantId, String sku) {
    }
}
