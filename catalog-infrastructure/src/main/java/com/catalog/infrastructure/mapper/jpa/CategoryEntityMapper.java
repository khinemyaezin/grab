package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.Category;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.entity.meta.CategoryEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public interface CategoryEntityMapper{
    @Mapping(ignore = true, target = CategoryEntity_.ID)
    @Mapping(source = "id", target = CategoryEntity_.UUID)
    void toEntity(Category source, @MappingTarget CategoryEntity entity);
}