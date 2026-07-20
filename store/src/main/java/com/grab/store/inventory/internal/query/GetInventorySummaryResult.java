package com.grab.store.inventory.internal.query;

public record GetInventorySummaryResult(
        String merchantId,
        String locationId,
        String locationCode,
        String locationName,
        long totalItems,
        StatusBreakdown status,
        HealthBreakdown health,
        QuantityTotals quantities
) {
    public record CountPercent(long count, double percent) {
    }

    public record StatusBreakdown(
            CountPercent active,
            CountPercent outOfStock,
            CountPercent suspended,
            CountPercent discontinued
    ) {
    }

    public record HealthBreakdown(
            long eligibleItems,
            CountPercent inStock,
            CountPercent lowStock,
            CountPercent outOfStock,
            CountPercent critical
    ) {
    }

    public record QuantityTotals(
            long onHand,
            long reserved,
            long inTransit,
            long damaged,
            long available
    ) {
    }
}
