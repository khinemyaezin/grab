package com.catalog.adapter.persistence.category.impl;

import com.catalog.adapter.persistence.category.CategoryNodeRemover;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaRemovingDelegate;
import com.nestedset.app.service.query.QueryBasedNestedSetNodeRemover;

public class CategoryNodeRemoverImpl extends QueryBasedNestedSetNodeRemover<CategoryEntity, Long> implements CategoryNodeRemover {

    public CategoryNodeRemoverImpl(CategoryJpaRemovingDelegate queryDelegate) {
        super(queryDelegate);
    }

}