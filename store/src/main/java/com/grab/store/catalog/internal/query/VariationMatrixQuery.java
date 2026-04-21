package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record VariationMatrixQuery(
        List<Variant> variants,
        List<VariantType> variantTypes
) implements Query<VariationMatrixResult> {

    public record VariantType(
            String typeId,
           List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId
    ) {}

    public record Variant(
            String matrixKey,
            String sku,
            List<Variation> variations
    ){}

    public record Variation(
            String optionId,
            String typeId
    ){}
}
