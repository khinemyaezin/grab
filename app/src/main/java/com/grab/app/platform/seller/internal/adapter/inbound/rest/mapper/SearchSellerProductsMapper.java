package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.SearchSellerProductsQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchSellerProductsMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "query", source = "query")
    @Mapping(target = "variantStatus", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "productStatus", ignore = true)
    @Mapping(target = "page", source = "page")
    @Mapping(target = "size", source = "size")
    public abstract SearchSellerProductsQuery toQuery(String merchantId, String query, int page, int size);

    public abstract ProductSummaryResponse toResponse(ProductSummaryOutboundResult result);

    public abstract List<ProductSummaryResponse> toResponseList(List<ProductSummaryOutboundResult> results);
}

