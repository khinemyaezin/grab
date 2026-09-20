package com.catalog.adapter.persistence.category.impl;

import com.catalog.adapter.persistence.category.CategoryNodeRetriever;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaRetrievingDelegate;
import com.nestedset.app.service.query.QueryBasedNestedSetNodeRetriever;

public class CategoryNodeRetrieverImpl extends QueryBasedNestedSetNodeRetriever<CategoryEntity, Long> implements CategoryNodeRetriever {

    public CategoryNodeRetrieverImpl(CategoryJpaRetrievingDelegate queryDelegate) {
        super(queryDelegate);
    }

}