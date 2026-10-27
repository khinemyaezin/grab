package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryLeavesResponse;
import com.catalog.application.model.read.CategoryLeavesResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = CategoryDtoMapper.class)
public abstract class CategoryLeavesDtoMapper {
    public abstract CategoryLeavesResponse toResponse(CategoryLeavesResult result);
}
