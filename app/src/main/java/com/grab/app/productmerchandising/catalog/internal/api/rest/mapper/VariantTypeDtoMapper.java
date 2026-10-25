package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response.VariantTypeResponse;
import com.catalog.application.model.read.VariantTypeResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class VariantTypeDtoMapper {
    public abstract VariantTypeResponse toResponse(VariantTypeResult result);
}
