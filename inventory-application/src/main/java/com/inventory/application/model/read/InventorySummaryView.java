package com.inventory.application.model.read;

public record InventorySummaryView(
        InventorySummaryScopeView scope,
        long totalItems,
        InventoryStatusBreakdownView status,
        InventoryStockHealthBreakdownView health,
        InventoryQuantityTotalsView quantities
) {
}
