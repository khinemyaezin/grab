package com.product.infrastructure.service;

import com.product.domain.entity.category.Category;
import com.product.infrastructure.entity.category.CategoryEntity;

import java.util.Optional;

public interface CategoryEntityService {
    Optional<CategoryEntity> find(String categoryUuid);
    CategoryEntity findOrCreateCategory(Category category);
    void save(CategoryEntity categoryEntity);
    void save(CategoryEntity parentCategoryEntity,CategoryEntity categoryEntity);
    void deleteCascade(CategoryEntity categoryEntity);
}
