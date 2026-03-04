package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetCategoryParentQuery(
        String categoryId
) implements Query<CategoryResult> {
}
