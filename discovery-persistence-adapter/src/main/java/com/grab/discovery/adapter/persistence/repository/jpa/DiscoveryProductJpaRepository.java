package com.grab.discovery.adapter.persistence.repository.jpa;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.application.model.read.DiscoveryProductKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiscoveryProductJpaRepository extends
        JpaRepository<DiscoveryProductEntity, Long>,
        JpaSpecificationExecutor<DiscoveryProductEntity> {

    Optional<DiscoveryProductEntity> findBySalesChannelIdAndProductId(String salesChannelId, String productId);

    Optional<DiscoveryProductEntity> findBySalesChannelIdAndSlugAndPublishedTrueAndChannelEnabledTrueAndProductStatus(
            String salesChannelId,
            String slug,
            String productStatus
    );

    List<DiscoveryProductEntity> findByProductId(String productId);

    List<DiscoveryProductEntity> findBySalesChannelId(String salesChannelId);

    @Query("SELECT new com.grab.discovery.application.model.read.DiscoveryProductKey(d.productId, d.salesChannelId) " +
            "FROM DiscoveryProductEntity d ORDER BY d.productId ASC, d.salesChannelId ASC")
    List<DiscoveryProductKey> findKeys(Pageable pageable);

    @Query("SELECT new com.grab.discovery.application.model.read.DiscoveryProductKey(d.productId, d.salesChannelId) " +
            "FROM DiscoveryProductEntity d " +
            "WHERE d.productId > :productId OR (d.productId = :productId AND d.salesChannelId > :channelId) " +
            "ORDER BY d.productId ASC, d.salesChannelId ASC")
    List<DiscoveryProductKey> findKeysAfter(
            @Param("productId") String productId,
            @Param("channelId") String channelId,
            Pageable pageable
    );

}
