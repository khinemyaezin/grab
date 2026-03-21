package com.catalog.infrastructure.repository.jpa.adapter;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaInsertingDelegate;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.delegate.jpa.JpaNestedSetInsertingDelegate;

public class CategoryJpaInsertingDelegateImpl extends JpaNestedSetInsertingDelegate<CategoryEntity, Long> implements CategoryJpaInsertingDelegate {

    public CategoryJpaInsertingDelegateImpl(JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> nestedSetRepositoryConfiguration) {
        super(nestedSetRepositoryConfiguration);
    }
}
