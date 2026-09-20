package com.catalog.application.model.read;


import com.grab.framework.cqrs.query.Query;

public record GetCategoryChildrenQuery(
        String categoryId
) implements Query<CategoryChildrenResult> {
}
