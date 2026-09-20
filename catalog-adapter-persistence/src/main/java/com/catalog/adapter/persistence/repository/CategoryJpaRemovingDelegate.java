package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.nestedset.app.delegate.NestedSetRemovingDelegate;

public interface CategoryJpaRemovingDelegate extends NestedSetRemovingDelegate<CategoryEntity, Long> {
}
