package com.grab.store.catalog.internal.api.rest.dto.response;

import java.util.List;

public record ProductSummaryResponse(
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
