package com.grab.store.catalog.internal.query;

import com.grab.store.catalog.internal.cqrs.query.Query;

public record GetProductQuery(
        String productId
) implements Query<GetProductResult> {
}
