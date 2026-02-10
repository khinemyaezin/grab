package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.product.internal.query.ProductSummaryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class ProductSummaryDtoMapper {

  public abstract ProductSummaryResponse toResponse(ProductSummaryResult result );
}
