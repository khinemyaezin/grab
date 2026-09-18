package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import com.inventory.infrastructure.entity.LocationEntity;

public interface ChannelFulfillmentRouteJpaAssembler {

    ChannelFulfillmentRouteEntity buildFullEntityGraph(
            ChannelFulfillmentRoute route,
            ChannelFulfillmentRouteEntity entity,
            LocationEntity location
    );

    ChannelFulfillmentRoute toFullDomainGraph(
            ChannelFulfillmentRouteEntity entity,
            LocationEntity location
    );
}
