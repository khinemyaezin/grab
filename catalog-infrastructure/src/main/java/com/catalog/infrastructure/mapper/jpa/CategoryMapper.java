package com.catalog.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.catalog.domain.aggregate.Category;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.entity.meta.CategoryEntity_;
import com.catalog.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class,uses = {IdGenerator.class})
public abstract class CategoryMapper {

    @Mapping(source = "entity." + CategoryEntity_.UUID, target="id")
    public abstract Category toDomain(CategoryEntity entity, String parentId);
}
