package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryResponse;
import com.catalog.application.model.read.CategoryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CategoryDtoMapper {
    public abstract CategoryResponse toResponse(CategoryResult result);
}
