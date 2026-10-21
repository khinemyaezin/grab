package com.grab.discovery.adapter.persistence.repository.jpa;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

    Page<DiscoveryProductEntity> findBySalesChannelIdAndPublishedTrueAndChannelEnabledTrueAndProductStatus(
            String salesChannelId,
            String productStatus,
            Pageable pageable
    );

    List<DiscoveryProductEntity> findByProductId(String productId);

    List<DiscoveryProductEntity> findBySalesChannelId(String salesChannelId);

    @Query("SELECT d FROM DiscoveryProductEntity d WHERE CAST(d.variants AS string) LIKE %:variantId%")
    List<DiscoveryProductEntity> findByVariantIdInVariants(@Param("variantId") String variantId);

    @Query("SELECT d FROM DiscoveryProductEntity d WHERE CAST(d.variants AS string) LIKE %:sku%")
    List<DiscoveryProductEntity> findBySkuInVariants(@Param("sku") String sku);
}
