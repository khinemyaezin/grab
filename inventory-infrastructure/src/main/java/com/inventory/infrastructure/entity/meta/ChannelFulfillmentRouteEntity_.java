package com.inventory.infrastructure.entity.meta;

import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ChannelFulfillmentRouteEntity.class)
public class ChannelFulfillmentRouteEntity_ {
    public static volatile SingularAttribute<ChannelFulfillmentRouteEntity, Long> locationId;
    public static volatile SingularAttribute<ChannelFulfillmentRouteEntity, String> salesChannelId;

    public static final String LOCATION_ID = "locationId";
    public static final String SALES_CHANNEL_ID = "salesChannelId";
}
