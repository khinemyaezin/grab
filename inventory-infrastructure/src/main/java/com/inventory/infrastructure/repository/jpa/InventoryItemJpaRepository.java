package com.inventory.infrastructure.repository.jpa;

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

    Optional<InventoryItemEntity> findBySku(String sku);

    Optional<InventoryItemEntity> findBySkuAndLocationId(String sku, String locationId);

    List<InventoryItemEntity> findAllBySku(String sku);

    List<InventoryItemEntity> findAllByLocationId(String locationId);

    @Query("SELECT i FROM InventoryItemEntity i WHERE i.sku = :sku AND i.onHand - i.reserved - i.damaged > 0")
    List<InventoryItemEntity> findAvailableInventoryBySku(@Param("sku") String sku);

    @Query("SELECT i FROM InventoryItemEntity i WHERE i.onHand - i.reserved - i.damaged <= i.reorderPoint")
    List<InventoryItemEntity> findItemsBelowReorderPoint();

    boolean existsBySkuAndLocationId(String sku, String locationId);
}
