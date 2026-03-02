package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryNodeResponse;
import com.grab.store.catalog.internal.query.CategoryNodeResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CategoryNodeDtoMapper {
    public abstract CategoryNodeResponse toResponse(CategoryNodeResult result);
}
