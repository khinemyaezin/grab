package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.ListSellerSalesChannelsQuery;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerSalesChannelsMapper {

    public abstract ListSellerSalesChannelsQuery toQuery(String merchantId, int page, int size);

    public abstract List<SalesChannelResponse> toResponse(List<ChannelOutboundResult> results);

    public abstract SalesChannelResponse toResponse(ChannelOutboundResult result);
}

