package com.inventory.adapter.persistence.repository.jpa;

import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntity;
import com.inventory.adapter.persistence.entity.ChannelFulfillmentRouteEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelFulfillmentRouteJpaRepository
        extends JpaRepository<ChannelFulfillmentRouteEntity, ChannelFulfillmentRouteEntityId> {

    Optional<ChannelFulfillmentRouteEntity> findByLocationIdAndSalesChannelId(Long locationId, String salesChannelId);

    List<ChannelFulfillmentRouteEntity> findByLocationId(Long locationId);

    boolean existsByLocationIdAndSalesChannelId(Long locationId, String salesChannelId);

    @Query("""
            select case when count(route) > 0 then true else false end
            from ChannelFulfillmentRouteEntity route
            join LocationEntity location on location.id = route.locationId
            where location.merchantId = :merchantId
              and location.active = true
              and route.salesChannelId = :salesChannelId
            """)
    boolean existsActiveForMerchantAndChannel(
            @Param("merchantId") String merchantId,
            @Param("salesChannelId") String salesChannelId
    );
}
