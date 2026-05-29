package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.VariantTypeResponse;
import com.grab.store.catalog.internal.query.VariantTypeResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class VariantTypeDtoMapper {
    public abstract VariantTypeResponse toResponse(VariantTypeResult result);
}
