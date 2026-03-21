package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.nestedset.app.delegate.NestedSetInsertingDelegate;

public interface CategoryJpaInsertingDelegate extends NestedSetInsertingDelegate<CategoryEntity, Long> {
}
