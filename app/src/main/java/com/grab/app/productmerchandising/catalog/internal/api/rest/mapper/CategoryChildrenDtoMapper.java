package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response.CategoryChildrenResponse;
import com.catalog.application.model.read.CategoryChildrenResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = CategoryDtoMapper.class)
public abstract class CategoryChildrenDtoMapper {
    public abstract CategoryChildrenResponse toResponse(CategoryChildrenResult result);
}
