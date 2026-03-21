package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.nestedset.app.delegate.NestedSetRetrievingDelegate;

import java.util.List;

public interface CategoryJpaRetrievingDelegate extends NestedSetRetrievingDelegate<CategoryEntity, Long> {
    List<CategoryEntity> getLeafNodesByName(String name);
}
