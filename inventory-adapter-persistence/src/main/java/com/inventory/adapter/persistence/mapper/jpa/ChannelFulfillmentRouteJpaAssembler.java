package com.inventory.adapter.persistence.mapper.jpa;

import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntity;
import com.inventory.adapter.persistence.entity.LocationEntity;

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
