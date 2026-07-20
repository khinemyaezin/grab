package com.inventory.infrastructure.view;

public record InventorySummaryAggregationView(
        long totalItems,
        long activeCount,
        long statusOutOfStockCount,
        long suspendedCount,
        long discontinuedCount,
        long healthEligibleItems,
        long healthInStock,
        long healthLowStock,
        long healthOutOfStock,
        long healthCritical,
        long onHand,
        long reserved,
        long inTransit,
        long damaged,
        long available
) {
}
