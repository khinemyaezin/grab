package com.inventory.adapter.persistence.repository.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.adapter.persistence.entity.InventoryItemEntity;
import com.inventory.application.model.read.DiscoveryInventoryAvailabilityView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemEntity, Long> {

    Optional<InventoryItemEntity> findByUuid(String uuid);

    Optional<InventoryItemEntity> findBySkuAndLocationId(String sku, String locationId);

    List<InventoryItemEntity> findAllByMerchantId(String merchantId);

    List<InventoryItemEntity> findAllBySku(String sku);

    List<InventoryItemEntity> findAllByProductVariantId(String productVariantId);

    List<InventoryItemEntity> findAllByLocationId(String locationId);

    List<InventoryItemEntity> findAllByStatusAndMerchantId(InventoryStatus status, String merchantId);

    @Query("SELECT i FROM InventoryItemEntity i WHERE (i.onHand - i.reserved - i.damaged) <= i.reorderPoint AND i.status = 'ACTIVE' AND i.merchantId = :merchantId")
    List<InventoryItemEntity> findLowStockItemsAndMerchantId(@Param("merchantId") String merchantId);

    boolean existsBySkuAndLocationId(String sku, String locationId);

    List<InventoryItemEntity> findAllByMerchantIdAndLocationIdAndSkuIn(
            String merchantId,
            String locationId,
            Collection<String> skus
    );

    @Query("SELECT new com.inventory.application.model.read.DiscoveryInventoryAvailabilityView(" +
            "COALESCE(SUM(CASE WHEN i.onHand - i.reserved - i.damaged > 0 " +
            "THEN i.onHand - i.reserved - i.damaged ELSE 0 END), 0L), " +
            "COALESCE(SUM(CASE WHEN i.allowBackorder = true THEN 1 ELSE 0 END), 0L)) " +
            "FROM InventoryItemEntity i, LocationEntity l, ChannelFulfillmentRouteEntity r " +
            "WHERE i.locationId = l.uuid AND r.locationId = l.id " +
            "AND i.merchantId = :merchantId AND l.merchantId = :merchantId AND i.sku = :sku " +
            "AND r.salesChannelId = :salesChannelId AND l.active = true " +
            "AND i.status IN :eligibleStatuses")
    DiscoveryInventoryAvailabilityView findDiscoveryAvailability(
            @Param("merchantId") String merchantId,
            @Param("sku") String sku,
            @Param("salesChannelId") String salesChannelId,
            @Param("eligibleStatuses") Collection<InventoryStatus> eligibleStatuses
    );
}
