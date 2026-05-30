package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.catalog.infrastructure.view.CategoryView;

import java.util.List;
import java.util.Optional;

/**
 * Read-side query adapter
 */
public interface CategoryQueryRepository {
    boolean exists(String categoryId);

    Optional<CategoryNodeView> findTree(String categoryId);

    Optional<CategoryChildrenView> findChildren(String categoryId);

    Optional<CategoryView> findParent(String categoryId);

    List<CategoryView> findLeafNodesByName(String name);

    List<CategoryView> findViewByIds(List<String> ids);
}
