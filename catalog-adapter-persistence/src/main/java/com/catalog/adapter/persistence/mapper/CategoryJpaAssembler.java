package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.Category;
import com.catalog.adapter.persistence.entity.CategoryEntity;

public interface CategoryJpaAssembler {
    CategoryEntity buildFullEntityGraph(Category category, CategoryEntity categoryEntity);

    Category buildFullDomainAggregate(CategoryEntity categoryEntity, CategoryEntity parentCategoryEntity);
}
