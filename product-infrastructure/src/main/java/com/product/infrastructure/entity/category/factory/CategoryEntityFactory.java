package com.product.infrastructure.entity.category.factory;

import com.product.domain.aggregate.category.Category;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.mapper.category.CategoryEntityMapper;
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
