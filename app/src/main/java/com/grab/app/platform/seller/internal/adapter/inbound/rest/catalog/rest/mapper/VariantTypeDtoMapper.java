package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariantTypeResponse;
import com.catalog.application.model.read.VariantTypeResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class VariantTypeDtoMapper {
    public abstract VariantTypeResponse toResponse(VariantTypeResult result);
}
