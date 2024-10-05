package com.coolstuff.ecommerce.grab.infrastructure.product.mapper.category;

import com.coolstuff.ecommerce.grab.domain.product.entity.category.PersistableCategory;
import com.coolstuff.ecommerce.grab.domain.product.generic.mapper.ObjectMapper;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper
public abstract class PersistableCategoryMapper implements ObjectMapper<PersistableCategory, CategoryEntity> {
    public abstract CategoryEntity convert(PersistableCategory source);
}
