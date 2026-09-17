package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import com.inventory.infrastructure.entity.meta.ChannelFulfillmentRouteEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class ChannelFulfillmentRouteEntityMapper {

    @Mapping(ignore = true, target = ChannelFulfillmentRouteEntity_.LOCATION_ID)
    @Mapping(source = "salesChannelId", target = ChannelFulfillmentRouteEntity_.SALES_CHANNEL_ID)
    public abstract void toEntity(ChannelFulfillmentRoute source, @MappingTarget ChannelFulfillmentRouteEntity destination);
}
