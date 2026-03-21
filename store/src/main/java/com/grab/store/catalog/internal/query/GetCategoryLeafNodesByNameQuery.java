package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetCategoryLeafNodesByNameQuery(
        String name
) implements Query<CategoryLeavesResult> {
}
