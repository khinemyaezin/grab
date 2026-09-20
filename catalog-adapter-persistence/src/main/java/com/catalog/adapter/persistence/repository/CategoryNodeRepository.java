package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.category.CategoryTreeNode;
import com.nestedset.app.NestedSetNodeRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
 * This is the low-level nested-set/tree infrastructure service.
 */
public interface CategoryNodeRepository {
    void insert(CategoryEntity entity, String parentUuid);

    Optional<CategoryEntity> findParent(CategoryEntity categoryEntity);

    Optional<CategoryEntity> findParent(String categoryUuid);

    Optional<CategoryTreeNode> findSubtree(String categoryUuid);

    List<CategoryEntity> findImmediateChildren(String categoryUuid);

    List<CategoryEntity> findLeafNodeByName(String name);

    Set<String> findSubtreeIds(String categoryUuid);

    void removeSubtree(String categoryUuid);
}
