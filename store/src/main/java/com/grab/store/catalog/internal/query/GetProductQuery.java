package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetProductQuery(
        String merchantId,
        String productId
) implements Query<GetProductResult> {
}
