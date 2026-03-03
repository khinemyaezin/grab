package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {

    Optional<InventoryItem> findById(Id id);

    List<InventoryItem> findBySku(String sku);

    Optional<InventoryItem> findBySkuAndLocation(String sku, Id locationId);

    List<InventoryItem> findByLocation(Id locationId);

    List<InventoryItem> findByProductVariantId(String productVariantId);

    List<InventoryItem> findLowStock();

    List<InventoryItem> findNeedsReorder();

    List<InventoryItem> findOutOfStock();

    int getTotalAvailableQuantityBySku(String sku);

    List<InventoryItem> findAll();

    void save(InventoryItem item);

    void delete(Id id);

    boolean existsBySkuAndLocation(String sku, Id locationId);
}
