package com.category.infrastructure.factory;

import com.category.domain.aggregate.Category;
import com.category.infrastructure.entity.CategoryEntity;
import com.category.infrastructure.mapper.CategoryEntityMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CategoryEntityFactory {
    final CategoryEntityMapper categoryEntityMapper;

    public CategoryEntity create(Category product) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntityMapper.map(product, categoryEntity);
        return categoryEntity;
    }
}
