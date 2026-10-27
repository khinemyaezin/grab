package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.ProductVariantSearchRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.ProductVariantSearchResponse;
import com.catalog.application.model.read.ProductVariantSummaryQuery;
import com.catalog.application.model.read.ProductVariantSummaryResult;
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
