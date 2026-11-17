package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.GetSellerProductBySlugQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSellerProductBySlugMapper {

    public abstract GetSellerProductBySlugQuery toQuery(String slug);

    public abstract ProductResponse toResponse(ProductOutboundResult result);

    public abstract VariantResponse toVariantResponse(VariantOutboundResult result);
}

