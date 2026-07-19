package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.InventoryStatus;

public record SearchInventoryRequest(
        String sku,
        String locationId,
        InventoryStatus status
) {
}
