package com.product.infrastructure.mapper.category;

import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.mapper.ObjectMapper;
import com.product.domain.entity.category.CategoryLeaf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class CategoryLeafMapper implements ObjectMapper<CategoryEntity, CategoryLeaf> {
    @Mapping(ignore = true, target = "parent")
    public abstract CategoryLeaf convert(CategoryEntity source);
}
