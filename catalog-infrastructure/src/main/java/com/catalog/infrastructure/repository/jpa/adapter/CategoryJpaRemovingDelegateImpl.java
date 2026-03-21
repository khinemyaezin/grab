package com.catalog.infrastructure.repository.jpa.adapter;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaInsertingDelegate;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRemovingDelegate;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.delegate.jpa.JpaNestedSetInsertingDelegate;
import com.nestedset.app.delegate.jpa.JpaNestedSetRemovingDelegate;

public class CategoryJpaRemovingDelegateImpl extends JpaNestedSetRemovingDelegate<CategoryEntity, Long> implements CategoryJpaRemovingDelegate {

    public CategoryJpaRemovingDelegateImpl(JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> nestedSetRepositoryConfiguration) {
        super(nestedSetRepositoryConfiguration);
    }
}
