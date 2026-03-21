package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.nestedset.app.delegate.NestedSetRemovingDelegate;

public interface CategoryJpaRemovingDelegate extends NestedSetRemovingDelegate<CategoryEntity, Long> {
}
