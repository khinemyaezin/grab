package com.catalog.adapter.persistence.mapper.impl;

import com.catalog.domain.aggregate.Category;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.mapper.CategoryEntityMapper;
import com.catalog.adapter.persistence.mapper.CategoryJpaAssembler;
import com.catalog.adapter.persistence.mapper.CategoryMapper;
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
            mergeCategoryEntity(category, categoryEntity);
        }
        return categoryEntity;
    }

    public CategoryEntity createCategoryEntity(Category category) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntityMapper.toEntity(category, categoryEntity);
        return categoryEntity;
    }

    private void mergeCategoryEntity(Category category, CategoryEntity categoryEntity) {
        categoryEntityMapper.toEntity(category, categoryEntity);
    }

    @Override
    public Category buildFullDomainAggregate(CategoryEntity categoryEntity, CategoryEntity parentCategoryEntity) {
        return categoryMapper.toDomain(categoryEntity, Objects.isNull(parentCategoryEntity) ? null: parentCategoryEntity.getUuid());
    }
}
