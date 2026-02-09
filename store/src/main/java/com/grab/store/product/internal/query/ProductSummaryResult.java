package com.grab.store.product.internal.query;

import com.grab.framework.cqrs.PageInfo;
import com.product.infrastructure.specification.jpa.ProductSummary;

import java.util.List;
import java.util.Map;

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
            Map<String, List<String>> options
    ) {}
}
