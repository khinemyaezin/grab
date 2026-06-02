package com.grab.store.catalog.internal.query;

import java.util.List;

public record GetProductResult(
        String id,
        String name,
        Category category,
        String condition,
        String status,
        String slug,
        List<Variant> variants,
        List<VariantType> variantTypes
) {

    public record Category(
            String id,
            String name
    ){}

    public record Variant(
            String id,
            String sku,
            String status,
            String matrixKey,
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
    ){}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}
}
