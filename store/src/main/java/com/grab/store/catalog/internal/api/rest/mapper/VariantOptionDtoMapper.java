package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import com.catalog.application.query.VariantOptionResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class VariantOptionDtoMapper {
    public abstract VariantOptionResponse toResponse(VariantOptionResult result);
}
