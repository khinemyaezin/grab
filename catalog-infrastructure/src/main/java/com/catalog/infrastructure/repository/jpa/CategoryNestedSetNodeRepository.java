package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.nestedset.app.NestedSetNodeRepository;

import java.util.List;

public interface CategoryNestedSetNodeRepository extends NestedSetNodeRepository<CategoryEntity, Long> {
    List<CategoryEntity> findLeafNodeBy(String name);
}
