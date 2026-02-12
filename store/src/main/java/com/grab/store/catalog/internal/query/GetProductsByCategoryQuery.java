package com.grab.store.catalog.internal.query;

import com.grab.store.catalog.internal.cqrs.query.Query;

public record GetProductsByCategoryQuery(
        String categoryId,
        int page,
        int size
) implements Query<ProductSummaryResult> {}
