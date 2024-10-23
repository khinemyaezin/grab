package com.product.infrastructure.mapper.category;

import com.product.domain.entity.category.ICategory;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.mapper.ObjectMapper;
import org.mapstruct.Mapper;

@Mapper
public abstract class CategoryEntityMapper implements ObjectMapper<ICategory, CategoryEntity> {
    public abstract CategoryEntity convert(ICategory source);
}
