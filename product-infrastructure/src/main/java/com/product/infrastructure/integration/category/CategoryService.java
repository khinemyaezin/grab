package com.product.infrastructure.integration.category;

import com.product.infrastructure.entity.category.CategoryEntity;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryEntity create(CategoryEntity entity);

    CategoryEntity create(CategoryEntity entity, String parentId);

    Optional<CategoryEntity> findBy(String id);

    CategoryEntity update(String uuid, CategoryEntity entity);

    void deleteCascade(String uuid);

    List<CategoryEntity> findImmediateCategory(String nodeId);

    List<CategoryEntity> findParentCategoryOf(String uuid);
}
