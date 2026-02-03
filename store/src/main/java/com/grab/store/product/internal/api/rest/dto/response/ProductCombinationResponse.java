package com.grab.store.product.internal.api.rest.dto.response;

import java.io.Serializable;
import java.util.List;

public record ProductCombinationResponse(
        Product product,
        List<VariantType> variantTypes
) implements Serializable {

    public record Product(
            String id,
            String name,
            String categoryId,
            List<Variant> variants
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

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionName,
            String optionId,
            String typeId,
            String typeName
    ) {}
}
