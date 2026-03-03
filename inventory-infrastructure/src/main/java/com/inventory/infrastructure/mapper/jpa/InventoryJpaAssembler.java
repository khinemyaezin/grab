package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.infrastructure.entity.InventoryItemEntity;

public interface InventoryJpaAssembler {
    /**
     * If `entity` is null a new InventoryItemEntity instance will be created; otherwise the provided `entity` is updated.
     *
     * @param inventoryItem the full InventoryItem aggregate to persist
     * @param entity the existing InventoryItemEntity to update, or null to create a new instance
     * @return the resulting InventoryItemEntity populated from the aggregate
     */
    InventoryItemEntity buildFullEntityGraph(InventoryItem inventoryItem, InventoryItemEntity entity);

    /**
     * Reconstruct the full InventoryItem domain aggregate from the provided JPA entity,
     *
     * @param inventoryItemEntity the persisted InventoryItemEntity to convert (may not be null)
     * @return a fully populated InventoryItem domain aggregate
     */
    InventoryItem toFullDomainGraph(InventoryItemEntity inventoryItemEntity);
}
