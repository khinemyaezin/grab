package com.product.infrastructure.mapper.category;

import com.grab.framework.mapper.CommonMapper;
import com.product.domain.aggregate.category.Category;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.entity.category.meta.CategoryEntity_;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = CommonMapper.class)
public interface CategoryEntityMapper{
    @Mapping(ignore = true, target = CategoryEntity_.ID)
    @Mapping(source = "id", target = CategoryEntity_.UUID)
    void map(Category source, @MappingTarget CategoryEntity entity);
}
