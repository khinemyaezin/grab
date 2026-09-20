package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.nestedset.app.NestedSetNodeRepository;

import java.util.List;

public interface CategoryNestedSetNodeRepository extends NestedSetNodeRepository<CategoryEntity, Long> {
    List<CategoryEntity> findLeafNodeBy(String name);
}
