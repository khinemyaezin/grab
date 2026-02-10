package com.grab.store.product.internal.api.rest.dto.response;

import com.grab.framework.cqrs.PageInfo;
import com.grab.store.product.internal.query.ProductSummaryResult;

import java.util.List;
import java.util.Map;

public record ProductSummaryResponse(
        List<Product> products,
        PageInfo pageInfo
) {
    public record Product(
            String id,
            String name,
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