package com.catalog.infrastructure.adapter.category.impl;

import com.catalog.infrastructure.adapter.category.CategoryNodeRetriever;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRetrievingDelegate;
import com.nestedset.app.service.query.QueryBasedNestedSetNodeRetriever;

public class CategoryNodeRetrieverImpl extends QueryBasedNestedSetNodeRetriever<CategoryEntity, Long> implements CategoryNodeRetriever {

    public CategoryNodeRetrieverImpl(CategoryJpaRetrievingDelegate queryDelegate) {
        super(queryDelegate);
    }

}