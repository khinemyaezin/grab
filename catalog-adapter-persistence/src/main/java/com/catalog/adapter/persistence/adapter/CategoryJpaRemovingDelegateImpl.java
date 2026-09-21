package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaInsertingDelegate;
import com.catalog.adapter.persistence.repository.CategoryJpaRemovingDelegate;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.delegate.jpa.JpaNestedSetInsertingDelegate;
import com.nestedset.app.delegate.jpa.JpaNestedSetRemovingDelegate;

public class CategoryJpaRemovingDelegateImpl extends JpaNestedSetRemovingDelegate<CategoryEntity, Long> implements CategoryJpaRemovingDelegate {

    public CategoryJpaRemovingDelegateImpl(JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> nestedSetRepositoryConfiguration) {
        super(nestedSetRepositoryConfiguration);
    }
}
