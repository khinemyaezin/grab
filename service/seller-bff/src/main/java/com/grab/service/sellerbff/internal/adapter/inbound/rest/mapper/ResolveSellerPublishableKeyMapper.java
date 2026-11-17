package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.KeyResolutionResponse;
import com.grab.service.sellerbff.internal.query.model.SellerSalesChannelQueries.ResolveSellerPublishableKeyQuery;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ResolveSellerPublishableKeyMapper {

    public abstract ResolveSellerPublishableKeyQuery toQuery(String rawToken);

    public abstract KeyResolutionResponse toResponse(KeyResolutionOutboundResult result);
}

