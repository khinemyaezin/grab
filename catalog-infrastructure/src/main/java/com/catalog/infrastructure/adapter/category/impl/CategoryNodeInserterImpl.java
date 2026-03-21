package com.catalog.infrastructure.adapter.category.impl;

import com.catalog.infrastructure.adapter.category.CategoryNodeInserter;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRetrievingDelegate;
import com.nestedset.app.delegate.NestedSetInsertingDelegate;
import com.nestedset.app.service.query.QueryBasedNestedSetNodeInserter;

public class CategoryNodeInserterImpl extends QueryBasedNestedSetNodeInserter<CategoryEntity, Long> implements CategoryNodeInserter {

    public CategoryNodeInserterImpl(NestedSetInsertingDelegate<CategoryEntity, Long> queryDelegate) {
        super(queryDelegate);
    }
}