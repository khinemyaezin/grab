package com.inventory.domain.factory;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.valueobject.ReorderConfig;

public interface InventoryItemFactory {

    InventoryItem create(
            String sku,
            Id productVariantId,
            Id locationId,
            int initialQuantity,
            ReorderConfig reorderConfig
    );

    InventoryItem create(
            String sku,
            Id productVariantId,
            Id locationId,
            int initialQuantity
    );

    InventoryItem createEmpty(
            String sku,
            Id productVariantId,
            Id locationId
    );
}
