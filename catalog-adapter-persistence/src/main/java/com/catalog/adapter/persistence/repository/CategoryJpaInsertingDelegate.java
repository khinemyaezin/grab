package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.nestedset.app.delegate.NestedSetInsertingDelegate;

public interface CategoryJpaInsertingDelegate extends NestedSetInsertingDelegate<CategoryEntity, Long> {
}
