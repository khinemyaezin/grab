package com.grab.store.product.internal.query;

import com.grab.store.product.internal.cqrs.query.Query;

import java.util.List;

public record ProductSummaryQuery(
        String productName,
        String sku,
        String variantStatus,
        List<String> variations,
        int page,
        int size
) implements Query<ProductSummaryResult> {
}
