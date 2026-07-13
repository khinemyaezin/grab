package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemEntity, Long> {

    Optional<InventoryItemEntity> findByUuid(String uuid);

    Optional<InventoryItemEntity> findBySkuAndLocationId(String sku, String locationId);

    List<InventoryItemEntity> findAllByMerchantId(String merchantId);

    List<InventoryItemEntity> findAllBySku(String sku);

    List<InventoryItemEntity> findAllByLocationId(String locationId);

    List<InventoryItemEntity> findAllByStatusAndMerchantId(InventoryStatus status, String merchantId);

    @Query("SELECT i FROM InventoryItemEntity i WHERE (i.onHand - i.reserved - i.damaged) <= i.reorderPoint AND i.status = 'ACTIVE' AND i.merchantId = :merchantId")
    List<InventoryItemEntity> findLowStockItemsAndMerchantId(@Param("merchantId") String merchantId);

    boolean existsBySkuAndLocationId(String sku, String locationId);
}
