package com.catalog.application.port.outbound;

import com.catalog.application.model.read.CategoryChildrenView;
import com.catalog.application.model.read.CategoryNodeView;
import com.catalog.application.model.read.CategoryView;

import java.util.List;
import java.util.Optional;

public interface CategoryQueryPort {
    boolean exists(String categoryId);

    Optional<CategoryNodeView> findTree(String categoryId);

    Optional<CategoryChildrenView> findChildren(String categoryId);

    Optional<CategoryView> findParent(String categoryId);

    List<CategoryView> findLeafNodesByName(String name);

    List<CategoryView> findViewByIds(List<String> ids);

    List<CategoryNodeView> findRootTrees();
}
