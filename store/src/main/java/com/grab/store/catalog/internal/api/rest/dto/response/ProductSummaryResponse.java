package com.grab.store.catalog.internal.api.rest.dto.response;

import com.grab.framework.cqrs.PageInfo;

import java.util.List;

public record ProductSummaryResponse(
        List<Product> products,
        PageInfo pageInfo
) {
    public record Product(
            String id,
            String name,
            String status,
            String slug,
            boolean featured,
            VariantSummary variants
    ) {}

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