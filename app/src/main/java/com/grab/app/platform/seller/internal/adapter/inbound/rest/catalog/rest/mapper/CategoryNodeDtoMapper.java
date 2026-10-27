package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryNodeResponse;
import com.catalog.application.model.read.CategoryNodeResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CategoryNodeDtoMapper {
    public abstract CategoryNodeResponse toResponse(CategoryNodeResult result);
}
