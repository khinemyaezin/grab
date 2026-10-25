package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.productmerchandising.catalog.api.representation.GetProductResponse;
import com.catalog.application.model.read.GetProductResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetProductDtoMapper {
    public abstract GetProductResponse toResponse(GetProductResult result);
}
