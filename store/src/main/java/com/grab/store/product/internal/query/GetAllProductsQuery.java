package com.grab.store.product.internal.query;

import com.grab.store.product.internal.cqrs.query.Query;

public record GetAllProductsQuery() implements Query<GetAllProductsResult> {
}
