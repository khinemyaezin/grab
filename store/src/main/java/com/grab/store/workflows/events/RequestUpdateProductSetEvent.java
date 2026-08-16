package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record RequestUpdateProductSetEvent(
        String workflowId,
        String merchantId,
        String productId,
        String name,
        String categoryId,
        String condition,
        String slug,
        VariantSync variantSync,
        Instant occurredAt,
        int version
) implements Event {

    public enum VariantSyncIntent {
        LEAVE_AS_IS,
        FULL_SYNC,
        COLLAPSE_TO_STANDALONE
    }

    public record VariantSync(
            VariantSyncIntent intent,
            List<Variant> overrides,
            List<VariantType> variantTypes
    ) {
        public VariantSync {
            overrides = overrides == null ? List.of() : List.copyOf(overrides);
            variantTypes = variantTypes == null ? List.of() : List.copyOf(variantTypes);
        }
    }

    public record Variant(
            String sku,
            String matrixKey,
            List<Variation> variations
    ) {
        public Variant {
            variations = variations == null ? List.of() : List.copyOf(variations);
        }
    }

    public record Variation(
            String typeId,
            String optionId
    ) {
    }

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ) {
        public VariantType {
            options = options == null ? List.of() : List.copyOf(options);
        }
    }

    public record VariantOption(
            String optionId,
            String optionName
    ) {
    }
}
