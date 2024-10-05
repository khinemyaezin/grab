package com.coolstuff.ecommerce.grab.infrastructure.product.mapper.category;

import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryLeaf;
import com.coolstuff.ecommerce.grab.domain.product.generic.mapper.ObjectMapper;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper
public abstract class CategoryLeafMapper implements ObjectMapper<CategoryEntity, CategoryLeaf> {
    public abstract CategoryLeaf convert(CategoryEntity source);
}
