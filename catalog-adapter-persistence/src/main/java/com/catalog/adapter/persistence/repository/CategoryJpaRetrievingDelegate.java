package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.nestedset.app.delegate.NestedSetRetrievingDelegate;

import java.util.List;

public interface CategoryJpaRetrievingDelegate extends NestedSetRetrievingDelegate<CategoryEntity, Long> {
    List<CategoryEntity> getLeafNodesByName(String name);
}
