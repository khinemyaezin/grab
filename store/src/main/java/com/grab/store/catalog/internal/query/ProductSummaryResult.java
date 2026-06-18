package com.grab.store.catalog.internal.query;

import java.util.List;

public record ProductSummaryResult(
        String id,
        String name,
        String status,
        String slug,
        String categoryName,
        VariantSummary variant
) {
    public record VariantSummary(
            boolean available,
            List<VariantType> types
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
