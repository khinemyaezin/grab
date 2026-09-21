package com.inventory.adapter.persistence.mapper.jpa;

import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntity;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.adapter.persistence.mapper.CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(config = CentralMapperConfig.class)
public abstract class ChannelFulfillmentRouteMapper {

    @BeanMapping(ignoreByDefault = true)
    public abstract ChannelFulfillmentRoute toDomain(
            ChannelFulfillmentRouteEntity entity,
            LocationEntity location
    );

    @ObjectFactory
    protected ChannelFulfillmentRoute create(ChannelFulfillmentRouteEntity entity, LocationEntity location) {
        return ChannelFulfillmentRoute.restore(
                new CommonId(location.getUuid()),
                new CommonId(entity.getSalesChannelId())
        );
    }
}
