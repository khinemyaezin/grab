package com.inventory.adapter.persistence.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntity;
import com.inventory.adapter.persistence.entity.meta.ChannelFulfillmentRouteEntity_;
import com.inventory.adapter.persistence.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class ChannelFulfillmentRouteEntityMapper {

    @Mapping(ignore = true, target = ChannelFulfillmentRouteEntity_.LOCATION_ID)
    @Mapping(source = "salesChannelId", target = ChannelFulfillmentRouteEntity_.SALES_CHANNEL_ID)
    public abstract void toEntity(ChannelFulfillmentRoute source, @MappingTarget ChannelFulfillmentRouteEntity destination);
}
