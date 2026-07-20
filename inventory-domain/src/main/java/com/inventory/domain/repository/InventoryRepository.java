package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {

    Optional<InventoryItem> findById(Id id);

    List<InventoryItem> findAll(Id merchantId);

    List<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByProductVariantId(Id productVariantId);

    List<InventoryItem> findOutOfStock(Id merchantId);

    List<InventoryItem> findLowStock(Id merchantId);

    List<InventoryItem> findByLocation(Id locationId);

    Optional<InventoryItem> findBySkuAndLocation(String sku, Id locationId);

    void save(InventoryItem item);

    void delete(Id id);

    boolean existsBySkuAndLocation(String sku, Id locationId);

    int getTotalAvailableQuantityBySku(String sku);
}
