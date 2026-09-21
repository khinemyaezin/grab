package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetCategoryQuery(
        String categoryId
) implements Query<CategoryResult> {
}
