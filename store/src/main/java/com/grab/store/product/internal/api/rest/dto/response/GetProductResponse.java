package com.grab.store.product.internal.api.rest.dto.response;

import java.util.List;

public record GetProductResponse(
        String id,
        String name,
        String categoryId,
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
