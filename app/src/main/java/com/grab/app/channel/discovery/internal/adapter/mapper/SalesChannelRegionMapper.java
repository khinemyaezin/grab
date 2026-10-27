package com.grab.app.channel.discovery.internal.adapter.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.Optional;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SalesChannelRegionMapper {
    public Optional<Id> toDefaultRegion(Optional<Id> defaultRegion) {
        return defaultRegion;
    }
}
