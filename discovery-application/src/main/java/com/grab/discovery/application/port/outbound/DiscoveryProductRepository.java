package com.grab.discovery.application.port.outbound;

import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public interface DiscoveryProductRepository {
    Optional<DiscoveryProductProjection> findBySalesChannelAndProduct(Id salesChannelId, Id productId);

    List<DiscoveryProductProjection> findByVariant(Id variantId);

    List<DiscoveryProductProjection> findByProduct(Id productId);

    List<DiscoveryProductProjection> findBySku(String sku);

    List<DiscoveryProductProjection> findBySalesChannel(Id salesChannelId);

    void save(DiscoveryProductProjection projection);
}
