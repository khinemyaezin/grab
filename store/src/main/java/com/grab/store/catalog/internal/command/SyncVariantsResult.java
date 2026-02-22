package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;

import java.util.List;

public record SyncVariantsResult(
        Id productId,
        String productName,
        List<Variant> variants,
        List<VariantType> variantTypes
) {

    public record Variant(
            Id id,
            String sku,
            String status,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionName,
            Id optionId,
            Id typeId,
            String typeName
    ) {}

    public record VariantType(
            Id typeId,
            String typeName,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            Id optionId,
            String optionName
    ) {}
}
