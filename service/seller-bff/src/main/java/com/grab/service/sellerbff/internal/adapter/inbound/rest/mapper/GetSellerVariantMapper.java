package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.GetSellerVariantQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSellerVariantMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "variantId", source = "variantId")
    public abstract GetSellerVariantQuery toQuery(String merchantId, String productId, String variantId);

    public abstract VariantResponse toResponse(VariantOutboundResult result);
}

