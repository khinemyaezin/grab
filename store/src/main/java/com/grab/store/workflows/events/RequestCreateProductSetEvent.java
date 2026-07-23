package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record RequestCreateProductSetEvent(
        String workflowId,
        String merchantId,
        Product product,
        List<VariantType> variantTypes,
        Instant occurredAt,
        int version
) implements Event {

    public record Product(
            String name,
            String categoryId,
            String condition,
            String slug,
            List<Variant> variants
    ) {
    }

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ) {
    }

    public record VariantOption(
            String optionId
    ) {
    }

    public record Variant(
            String sku,
            List<Variation> variations
    ) {
    }

    public record Variation(
            String optionId,
            String typeId
    ) {
    }
}
