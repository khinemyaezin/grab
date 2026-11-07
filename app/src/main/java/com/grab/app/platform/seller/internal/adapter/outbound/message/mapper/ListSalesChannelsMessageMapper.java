package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.SalesChannelManagementPort;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListSalesChannelsMessageMapper {

    public abstract List<ChannelOutboundResult> toResultList(List<SalesChannelManagementPort.ChannelMessageResult> results);

    public abstract ChannelOutboundResult toResult(SalesChannelManagementPort.ChannelMessageResult result);
}

