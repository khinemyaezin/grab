package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryChildrenResponse;
import com.grab.store.catalog.internal.query.CategoryChildrenResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = CategoryDtoMapper.class)
public abstract class CategoryChildrenDtoMapper {
    public abstract CategoryChildrenResponse toResponse(CategoryChildrenResult result);
}
