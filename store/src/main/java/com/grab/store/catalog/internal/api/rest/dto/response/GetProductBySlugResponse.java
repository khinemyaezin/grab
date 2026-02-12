package com.grab.store.catalog.internal.api.rest.dto.response;

import java.util.List;

public record GetProductBySlugResponse(
        String id,
        String name,
        String categoryId,
        String status,
        String slug,
        boolean featured,
        List<Variant> variants,
        List<VariantType> variantTypes
) {
    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionId,
            String optionName,
            String typeId,
            String typeName
    ) {}

    public record VariantType(
            String typeId,
            String typeName,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}
}
