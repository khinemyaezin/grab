package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import com.grab.store.catalog.internal.query.GetProductBySlugResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class GetProductBySlugDtoMapper {
    public abstract GetProductBySlugResponse toResponse(GetProductBySlugResult result);
}
