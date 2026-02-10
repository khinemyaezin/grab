package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.response.GetProductResponse;
import com.grab.store.product.internal.query.GetProductResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class GetProductDtoMapper {
    public abstract GetProductResponse toResponse(GetProductResult result);
}
