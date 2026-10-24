package com.grab.discovery.adapter.persistence.repository.jpa;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscoveryProductVariantJpaRepository extends JpaRepository<DiscoveryProductVariantEntity, Long> {
    @Modifying
    @Query("DELETE FROM DiscoveryProductVariantEntity v WHERE v.discoveryProduct.id = :discoveryProductId")
    void deleteForProduct(@Param("discoveryProductId") Long discoveryProductId);

    @EntityGraph(attributePaths = "discoveryProduct")
    List<DiscoveryProductVariantEntity> findByVariantId(String variantId);

    @EntityGraph(attributePaths = "discoveryProduct")
    List<DiscoveryProductVariantEntity> findBySku(String sku);

    boolean existsByDiscoveryProduct_IdAndVariantStatus(Long discoveryProductId, String variantStatus);
}
