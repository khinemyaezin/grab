package com.grab.store.catalog.internal.query;

import com.grab.framework.id.Id;

import java.util.List;

public record VariationMatrixResult(
        List<Variant> variants,
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
            List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId
    ) {}

    public record Variant(
            String matrixKey,
            List<Variation> variations
    ){}

    public record Variation(
            String optionId,
            String typeId
    ){}
}
