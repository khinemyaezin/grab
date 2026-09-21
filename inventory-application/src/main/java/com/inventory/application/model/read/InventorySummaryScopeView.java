package com.inventory.application.model.read;

public record InventorySummaryScopeView(
        String merchantId,
        String locationId,
        String locationCode,
        String locationName
) {
}
