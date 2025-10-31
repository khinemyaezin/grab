package com.category.infrastructure.service;

import com.category.domain.aggregate.Category;
import com.category.infrastructure.entity.CategoryEntity;

import java.util.Optional;

public interface CategoryService {
    Optional<CategoryEntity> find(String categoryUuid);
    CategoryEntity findOrBuildCategory(Category category);
    void save(CategoryEntity categoryEntity);
    void save(CategoryEntity parentCategoryEntity,CategoryEntity categoryEntity);
    void deleteCascade(CategoryEntity categoryEntity);
}
