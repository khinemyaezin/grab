package com.inventory.infrastructure.view;

public record InventorySummaryView(
        InventorySummaryScopeView scope,
        long totalItems,
        InventoryStatusBreakdownView status,
        InventoryStockHealthBreakdownView health,
        InventoryQuantityTotalsView quantities
) {
}
