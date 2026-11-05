package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.PublishableApiKeyPort;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolvePublishableKeyMessageMapper {

    public abstract KeyResolutionOutboundResult toResult(PublishableApiKeyPort.KeyResolutionResult result);
}

