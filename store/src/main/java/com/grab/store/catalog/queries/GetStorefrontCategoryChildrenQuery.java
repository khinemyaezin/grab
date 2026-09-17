package com.grab.store.catalog.queries;

import com.grab.framework.cqrs.query.Query;

public record GetStorefrontCategoryChildrenQuery(
        String categoryId
) implements Query<StorefrontCategoryChildren> {
}
