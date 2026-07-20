package com.inventory.infrastructure.view;

public record InventorySummaryScopeView(
        String merchantId,
        String locationId,
        String locationCode,
        String locationName
) {
}
