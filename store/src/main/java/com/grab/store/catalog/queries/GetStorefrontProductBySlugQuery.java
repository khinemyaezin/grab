package com.grab.store.catalog.queries;

import com.grab.framework.cqrs.query.Query;

public record GetStorefrontProductBySlugQuery(
        String slug
) implements Query<GetProductBySlugResult> {
}
