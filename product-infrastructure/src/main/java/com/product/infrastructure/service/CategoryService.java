package com.product.infrastructure.service;

import com.product.domain.aggregate.category.Category;
import com.product.infrastructure.entity.category.entity.CategoryEntity;

import java.util.Optional;

public interface CategoryService {
    Optional<CategoryEntity> find(String categoryUuid);
    CategoryEntity findOrBuildCategory(Category category);
    void save(CategoryEntity categoryEntity);
    void save(CategoryEntity parentCategoryEntity,CategoryEntity categoryEntity);
    void deleteCascade(CategoryEntity categoryEntity);
}
