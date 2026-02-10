package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.Category;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.mapper.jpa.CategoryEntityMapper;
import com.catalog.infrastructure.mapper.jpa.CategoryJpaAssembler;
import com.catalog.infrastructure.mapper.jpa.CategoryMapper;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class CategoryJpaAssemblerImpl implements CategoryJpaAssembler {
    private final CategoryEntityMapper categoryEntityMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryEntity buildFullEntityGraph(Category category, CategoryEntity categoryEntity) {
        if(categoryEntity == null) {
            categoryEntity = createCategoryEntity(category);
        } else {
            categoryEntity = mergeCategoryEntity(category, categoryEntity);
        }
        return categoryEntity;
    }

    public CategoryEntity createCategoryEntity(Category category) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntityMapper.toEntity(category, categoryEntity);
        return categoryEntity;
    }

    private CategoryEntity mergeCategoryEntity(Category category, CategoryEntity categoryEntity) {
        categoryEntityMapper.toEntity(category, categoryEntity);
        return categoryEntity;
    }

    @Override
    public Category buildFullDomainAggregate(CategoryEntity categoryEntity, CategoryEntity parentCategoryEntity) {
        return categoryMapper.toDomain(categoryEntity, Objects.isNull(parentCategoryEntity) ? null: parentCategoryEntity.getUuid());
    }
}
