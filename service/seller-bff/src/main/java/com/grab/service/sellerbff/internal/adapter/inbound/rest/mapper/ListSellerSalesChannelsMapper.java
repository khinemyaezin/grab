package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.service.sellerbff.internal.query.model.SellerSalesChannelQueries.ListSellerSalesChannelsQuery;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerSalesChannelsMapper {

    public abstract ListSellerSalesChannelsQuery toQuery(String merchantId, int page, int size);

    public abstract List<SalesChannelResponse> toResponse(List<ChannelOutboundResult> results);

    public abstract SalesChannelResponse toResponse(ChannelOutboundResult result);
}

