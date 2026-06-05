package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.view.InventoryItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    List<InventoryItemEntity> findAllBySellerId(String sellerId);

    List<InventoryItemEntity> findAllBySku(String sku);

    List<InventoryItemEntity> findAllByLocationId(String locationId);

    List<InventoryItemEntity> findAllByStatusAndSellerId(InventoryStatus status, String sellerId);

    @Query("SELECT i FROM InventoryItemEntity i WHERE (i.onHand - i.reserved - i.damaged) <= i.reorderPoint AND i.status = 'ACTIVE' AND i.sellerId = :sellerId")
    List<InventoryItemEntity> findLowStockItemsAndSellerId(@Param("sellerId") String sellerId);

    boolean existsBySkuAndLocationId(String sku, String locationId);
}
