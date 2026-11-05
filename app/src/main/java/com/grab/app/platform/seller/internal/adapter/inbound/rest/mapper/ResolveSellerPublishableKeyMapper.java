package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.KeyResolutionResponse;
import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.ResolveSellerPublishableKeyQuery;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ResolveSellerPublishableKeyMapper {

    public abstract ResolveSellerPublishableKeyQuery toQuery(String rawToken);

    public abstract KeyResolutionResponse toResponse(KeyResolutionOutboundResult result);
}

