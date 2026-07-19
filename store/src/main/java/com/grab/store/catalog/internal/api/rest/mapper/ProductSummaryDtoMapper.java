package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSearchRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSearchResponse;
import com.grab.store.catalog.internal.query.ProductSearchQuery;
import com.grab.store.catalog.internal.query.ProductSearchResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProductSummaryDtoMapper {

  public abstract ProductSearchQuery toQuery(String merchantId, ProductSearchRequest request, Pageable pageable);

  public abstract ProductSearchResponse toResponse(ProductSearchResult result );
}
