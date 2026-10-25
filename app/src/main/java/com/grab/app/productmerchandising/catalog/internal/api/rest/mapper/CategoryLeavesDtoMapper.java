package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response.CategoryLeavesResponse;
import com.catalog.application.model.read.CategoryLeavesResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = CategoryDtoMapper.class)
public abstract class CategoryLeavesDtoMapper {
    public abstract CategoryLeavesResponse toResponse(CategoryLeavesResult result);
}
