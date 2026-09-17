package com.grab.store.catalog.queries;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record GetStorefrontCategoryTreeQuery(
        String rootId
) implements Query<List<StorefrontCategoryNode>> {
}
