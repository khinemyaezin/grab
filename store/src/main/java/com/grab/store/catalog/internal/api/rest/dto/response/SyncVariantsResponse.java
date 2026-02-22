package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;
import java.util.List;

public record SyncVariantsResponse(
        String productId,
        String productName,
        List<Variant> variants,
        List<VariantType> variantTypes
) implements Serializable {

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
