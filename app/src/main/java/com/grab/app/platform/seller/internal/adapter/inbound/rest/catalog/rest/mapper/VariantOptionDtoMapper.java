package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariantOptionResponse;
import com.catalog.application.model.read.VariantOptionResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class VariantOptionDtoMapper {
    public abstract VariantOptionResponse toResponse(VariantOptionResult result);
}
