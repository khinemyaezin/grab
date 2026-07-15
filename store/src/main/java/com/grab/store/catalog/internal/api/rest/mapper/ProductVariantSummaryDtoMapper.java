package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductVariantSearchRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductVariantSearchResponse;
import com.grab.store.catalog.internal.query.ProductVariantSummaryQuery;
import com.grab.store.catalog.internal.query.ProductVariantSummaryResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProductVariantSummaryDtoMapper {

    public abstract ProductVariantSummaryQuery toQuery(
            String merchantId,
            ProductVariantSearchRequest request,
            Pageable pageable
    );

    public abstract ProductVariantSearchResponse toResponse(ProductVariantSummaryResult result);
}
