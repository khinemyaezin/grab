package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;

import java.util.List;
import java.util.Optional;

public interface ChannelFulfillmentRouteRepository {
    Optional<ChannelFulfillmentRoute> find(Id locationId, Id salesChannelId);

    List<ChannelFulfillmentRoute> findByLocationId(Id locationId);

    boolean exists(Id locationId, Id salesChannelId);

    boolean existsActiveForMerchantAndChannel(Id merchantId, Id salesChannelId);

    void save(ChannelFulfillmentRoute route);

    void delete(ChannelFulfillmentRoute route);
}
