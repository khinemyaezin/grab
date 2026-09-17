package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteEntityMapper;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChannelFulfillmentRouteJpaAssemblerImpl implements ChannelFulfillmentRouteJpaAssembler {
    private final ChannelFulfillmentRouteEntityMapper entityMapper;
    private final ChannelFulfillmentRouteMapper domainMapper;

    @Override
    public ChannelFulfillmentRouteEntity buildFullEntityGraph(
            ChannelFulfillmentRoute route,
            ChannelFulfillmentRouteEntity entity,
            LocationEntity location
    ) {
        if (entity == null) {
            entity = new ChannelFulfillmentRouteEntity();
        }
        entity.setLocationId(location.getId());
        entityMapper.toEntity(route, entity);
        return entity;
    }

    @Override
    public ChannelFulfillmentRoute toFullDomainGraph(
            ChannelFulfillmentRouteEntity entity,
            LocationEntity location
    ) {
        return domainMapper.toDomain(entity, location);
    }
}
