package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.catalog.queries.GetProductBySlugResult;

public record GetProductBySlugQuery(
        String slug
) implements Query<GetProductBySlugResult> {}
