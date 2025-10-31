package com.category.infrastructure.mapper;

import com.category.domain.aggregate.Category;
import com.category.infrastructure.entity.CategoryEntity;
import com.category.infrastructure.meta.CategoryEntity_;
import com.grab.framework.mapper.CommonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = CommonMapper.class)
public interface CategoryEntityMapper{
    @Mapping(ignore = true, target = CategoryEntity_.ID)
    @Mapping(source = "id", target = CategoryEntity_.UUID)
    void map(Category source, @MappingTarget CategoryEntity entity);
}