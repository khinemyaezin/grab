package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetProductsByCategoryQuery(
        String categoryId,
        int page,
        int size
) implements Query<ProductSummaryResult> {}
