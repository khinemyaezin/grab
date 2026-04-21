package com.grab.store.catalog.internal.api.rest.dto.request;

import java.io.Serializable;
import java.util.List;

public record UpdateProductRequest(
        String name,
        String categoryId,
        String condition,
        String slug,
        VariantSync variantSync
) implements Serializable {

    public enum VariantSyncIntent {
        LEAVE_AS_IS,
        FULL_SYNC,
        COLLAPSE_TO_STANDALONE
    }

    public record VariantSync(
            VariantSyncIntent intent,
            List<Variant> overrides,
            List<VariantType> variantTypes
    ) {}

    public record Variant(
            String sku,
            String matrixKey,
            List<Variation> variations
    ) {}

    public record Variation(
            String typeId,
            String optionId
    ){}

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}
}
