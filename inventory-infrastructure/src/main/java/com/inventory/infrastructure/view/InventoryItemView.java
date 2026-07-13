package com.inventory.infrastructure.view;

import com.inventory.domain.enums.InventoryStatus;

import java.time.LocalDateTime;

public record InventoryItemView(
        String uuid,
        String sku,
        String merchantId,
        String productVariantId,
        String locationId,
        int onHand,
        int reserved,
        int inTransit,
        int damaged,
        int safetyStock,
        int reorderPoint,
        int reorderQuantity,
        Integer maxStock,
        InventoryStatus status,
        LocalDateTime lastUpdated
) {
}
