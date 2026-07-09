package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProductSummaryDtoMapper {

  public abstract ProductSummaryQuery toQuery(String merchantId, ProductSummaryRequest request, Pageable pageable);

  public abstract ProductSummaryResponse toResponse(ProductSummaryResult result );
}
