package com.catalog.adapter.persistence.category.impl;

import com.catalog.adapter.persistence.category.CategoryNodeInserter;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaRetrievingDelegate;
import com.nestedset.app.delegate.NestedSetInsertingDelegate;
import com.nestedset.app.service.query.QueryBasedNestedSetNodeInserter;

public class CategoryNodeInserterImpl extends QueryBasedNestedSetNodeInserter<CategoryEntity, Long> implements CategoryNodeInserter {

    public CategoryNodeInserterImpl(NestedSetInsertingDelegate<CategoryEntity, Long> queryDelegate) {
        super(queryDelegate);
    }
}