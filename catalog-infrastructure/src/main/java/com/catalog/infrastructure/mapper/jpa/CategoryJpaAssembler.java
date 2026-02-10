package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.Category;
import com.catalog.infrastructure.entity.entity.CategoryEntity;

public interface CategoryJpaAssembler {
    CategoryEntity buildFullEntityGraph(Category category, CategoryEntity categoryEntity);

    Category buildFullDomainAggregate(CategoryEntity categoryEntity, CategoryEntity parentCategoryEntity);
}
