package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.Category;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.entity.meta.CategoryEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public interface CategoryEntityMapper{
    @Mapping(ignore = true, target = CategoryEntity_.ID)
    @Mapping(source = "id", target = CategoryEntity_.UUID)
    @Mapping(source = "active", target = CategoryEntity_.ACTIVE)
    @Mapping(source = "listingAllowed", target = CategoryEntity_.LISTING_ALLOWED)
    @Mapping(source = "c2cAllowed", target = CategoryEntity_.C2C_ALLOWED)
    void toEntity(Category source, @MappingTarget CategoryEntity entity);
}
