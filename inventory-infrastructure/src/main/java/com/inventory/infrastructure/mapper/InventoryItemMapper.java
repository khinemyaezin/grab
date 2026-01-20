package com.inventory.infrastructure.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InventoryItemMapper {
    private final IdGenerator idGenerator;

    public InventoryItem toDomain(InventoryItemEntity entity) {
        if (entity == null) {
            return null;
        }

        Id id = idGenerator.generateId(entity.getUuid());
        Id productVariantId = entity.getProductVariantId() != null ? idGenerator.generateId(entity.getProductVariantId()) : null;
        Id locationId = idGenerator.generateId(entity.getLocationId());

        InventoryQuantity quantity = new InventoryQuantity(
                entity.getOnHand(),
                entity.getReserved(),
                entity.getInTransit(),
                entity.getDamaged()
        );

        ReorderConfig reorderConfig = new ReorderConfig(
                entity.getSafetyStock(),
                entity.getReorderPoint(),
                entity.getReorderQuantity(),
                entity.getMaxStock()
        );

        return new InventoryItem(
                id,
                entity.getSku(),
                productVariantId,
                locationId,
                quantity,
                reorderConfig,
                entity.getStatus()
        );

    }
}
