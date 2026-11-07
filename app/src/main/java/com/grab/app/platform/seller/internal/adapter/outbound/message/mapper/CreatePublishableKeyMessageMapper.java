package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.PublishableApiKeyPort;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.CreatePublishableKeyRecord;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePublishableKeyMessageMapper {

    public abstract PublishableApiKeyPort.CreatePublishableKeyMessage toMessage(CreatePublishableKeyRecord record);

    public abstract PublishableKeyOutboundResult toResult(PublishableApiKeyPort.PublishableKeyResult result);
}

