package com.catalog.infrastructure.adapter.category.impl;

import com.catalog.infrastructure.adapter.category.CategoryNodeRemover;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRemovingDelegate;
import com.nestedset.app.service.query.QueryBasedNestedSetNodeRemover;

public class CategoryNodeRemoverImpl extends QueryBasedNestedSetNodeRemover<CategoryEntity, Long> implements CategoryNodeRemover {

    public CategoryNodeRemoverImpl(CategoryJpaRemovingDelegate queryDelegate) {
        super(queryDelegate);
    }

}