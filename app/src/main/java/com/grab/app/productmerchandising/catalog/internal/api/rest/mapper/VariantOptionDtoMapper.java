package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import com.catalog.application.model.read.VariantOptionResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class VariantOptionDtoMapper {
    public abstract VariantOptionResponse toResponse(VariantOptionResult result);
}
