package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.service.sellerbff.internal.query.model.SellerSalesChannelQueries.GetSellerSalesChannelQuery;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerSalesChannelMapper {

    public abstract GetSellerSalesChannelQuery toQuery(String salesChannelId, String merchantId);

    public abstract SalesChannelResponse toResponse(ChannelOutboundResult result);
}

