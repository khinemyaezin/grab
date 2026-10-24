package com.grab.store.storefrontquery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.saleschannel.port.PublishableKeyQuery;
import com.grab.store.storefrontquery.internal.api.rest.mapper.CentralMapperConfig;
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
