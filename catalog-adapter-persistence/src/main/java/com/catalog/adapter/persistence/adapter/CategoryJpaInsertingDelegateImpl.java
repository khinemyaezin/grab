package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaInsertingDelegate;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.delegate.jpa.JpaNestedSetInsertingDelegate;

public class CategoryJpaInsertingDelegateImpl extends JpaNestedSetInsertingDelegate<CategoryEntity, Long> implements CategoryJpaInsertingDelegate {

    public CategoryJpaInsertingDelegateImpl(JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> nestedSetRepositoryConfiguration) {
        super(nestedSetRepositoryConfiguration);
    }
}
