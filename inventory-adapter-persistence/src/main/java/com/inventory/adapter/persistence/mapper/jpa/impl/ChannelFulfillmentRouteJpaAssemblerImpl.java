package com.inventory.adapter.persistence.mapper.jpa.impl;

import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntity;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.adapter.persistence.mapper.jpa.ChannelFulfillmentRouteEntityMapper;
import com.inventory.adapter.persistence.mapper.jpa.ChannelFulfillmentRouteJpaAssembler;
import com.inventory.adapter.persistence.mapper.jpa.ChannelFulfillmentRouteMapper;
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
