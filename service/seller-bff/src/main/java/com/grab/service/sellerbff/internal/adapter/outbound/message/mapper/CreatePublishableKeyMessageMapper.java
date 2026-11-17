package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.channel.saleschannel.PublishableApiKeyPort;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.CreatePublishableKeyRecord;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePublishableKeyMessageMapper {

    public abstract PublishableApiKeyPort.CreatePublishableKeyMessage toMessage(CreatePublishableKeyRecord record);

    public abstract PublishableKeyOutboundResult toResult(PublishableApiKeyPort.PublishableKeyResult result);
}

