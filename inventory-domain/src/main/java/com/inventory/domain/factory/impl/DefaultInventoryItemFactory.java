package com.inventory.domain.factory.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.factory.InventoryItemFactory;
import com.inventory.domain.valueobject.ReorderConfig;

public class DefaultInventoryItemFactory implements InventoryItemFactory {

    private final IdGenerator idGenerator;

    public DefaultInventoryItemFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public InventoryItem create(
            String sku,
            Id productVariantId,
            Id locationId,
            int initialQuantity,
            ReorderConfig reorderConfig
    ) {
        return InventoryItem.create(
                idGenerator.generateId(),
                sku,
                productVariantId,
                locationId,
                initialQuantity,
                reorderConfig
        );
    }

    @Override
    public InventoryItem create(
            String sku,
            Id productVariantId,
            Id locationId,
            int initialQuantity
    ) {
        return create(sku, productVariantId, locationId, initialQuantity, ReorderConfig.defaultConfig());
    }

    @Override
    public InventoryItem createEmpty(
            String sku,
            Id productVariantId,
            Id locationId
    ) {
        return create(sku, productVariantId, locationId, 0, ReorderConfig.defaultConfig());
    }
}
