package com.grab.app.channel.discovery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.channel.saleschannel.port.PublishableKeyQuery;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SalesChannelPublishableKeyMapper {
    @Mapping(target = "salesChannelId", source = "salesChannelId")
    @Mapping(target = "enabled", source = "channelEnabled")
    public abstract SalesChannelPublishableKeyPort.ResolvedChannel toResolvedChannel(
            PublishableKeyQuery.Resolution resolution
    );
}
