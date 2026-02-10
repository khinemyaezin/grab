package com.grab.store.product.internal.query;

import com.grab.framework.cqrs.PageInfo;

import java.util.List;

public record ProductSummaryResult(
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
