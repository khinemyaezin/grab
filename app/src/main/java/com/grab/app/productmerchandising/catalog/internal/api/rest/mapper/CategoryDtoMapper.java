package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response.CategoryResponse;
import com.catalog.application.model.read.CategoryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CategoryDtoMapper {
    public abstract CategoryResponse toResponse(CategoryResult result);
}
