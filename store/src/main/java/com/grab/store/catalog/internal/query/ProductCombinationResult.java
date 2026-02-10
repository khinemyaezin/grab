package com.grab.store.catalog.internal.query;

import com.grab.framework.id.Id;

import java.util.List;

public record ProductCombinationResult(
        Product product,
        List<VariantType> variantTypes
) {

    public record Product(
            Id id,
            String name,
            Id categoryId,
            List<Variant> variants
    ){}

    public record VariantType(
            String typeId,
            String typeName,
            List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
    ){}

    public record Variation(
            String optionName,
            String optionId,
            String typeId,
            String typeName
    ){}
}
