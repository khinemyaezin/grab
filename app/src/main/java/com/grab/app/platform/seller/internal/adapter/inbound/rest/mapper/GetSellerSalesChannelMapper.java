package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.GetSellerSalesChannelQuery;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerSalesChannelMapper {

    public abstract GetSellerSalesChannelQuery toQuery(String salesChannelId, String merchantId);

    public abstract SalesChannelResponse toResponse(ChannelOutboundResult result);
}

