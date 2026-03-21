package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryLeavesResponse;
import com.grab.store.catalog.internal.query.CategoryLeavesResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = CategoryDtoMapper.class)
public abstract class CategoryLeavesDtoMapper {
    public abstract CategoryLeavesResponse toResponse(CategoryLeavesResult result);
}
