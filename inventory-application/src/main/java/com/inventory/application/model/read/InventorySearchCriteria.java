package com.inventory.application.model.read;

import com.inventory.domain.enums.InventoryStatus;

public record InventorySearchCriteria(
        String merchantId,
        String sku,
        String locationId,
        InventoryStatus status,
        String variantId
) {
}
