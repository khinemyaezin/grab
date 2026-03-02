package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryResponse;
import com.grab.store.catalog.internal.query.CategoryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CategoryDtoMapper {
    public abstract CategoryResponse toResponse(CategoryResult result);
}
