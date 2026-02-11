package com.grab.store.catalog.internal.query;

import com.grab.store.catalog.internal.cqrs.query.Query;

public record GetProductBySlugQuery(
        String slug
) implements Query<GetProductBySlugResult> {}
