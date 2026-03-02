package com.grab.store.catalog.internal.query;

import com.grab.store.catalog.internal.cqrs.query.Query;

public record GetCategoryChildrenQuery(
        String categoryId
) implements Query<CategoryChildrenResult> {
}
