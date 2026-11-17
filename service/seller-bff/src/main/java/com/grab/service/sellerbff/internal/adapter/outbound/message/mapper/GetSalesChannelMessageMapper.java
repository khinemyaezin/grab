package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.channel.saleschannel.SalesChannelManagementPort;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSalesChannelMessageMapper {

    public abstract ChannelOutboundResult toResult(SalesChannelManagementPort.ChannelMessageResult result);
}

