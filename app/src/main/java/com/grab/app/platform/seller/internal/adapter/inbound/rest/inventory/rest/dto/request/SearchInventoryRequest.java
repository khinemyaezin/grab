package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

import com.inventory.domain.enums.InventoryStatus;

public record SearchInventoryRequest(
        String sku,
        String locationId,
        InventoryStatus status,
        String variantId
) {
}
