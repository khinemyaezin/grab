package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;
import java.util.List;

public record VariationMatrixResponse(
        List<Variant> variants,
        List<VariantType> variantTypes
) implements Serializable {

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            String optionId
    ) {}

    public record Variant(
            String matrixKey,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionId,
            String typeId
    ) {}
}
