package com.inventory.infrastructure.view;

public record InventoryStockHealthBreakdownView(
        long eligibleItems,
        CountBucketView inStock,
        CountBucketView lowStock,
        CountBucketView outOfStock,
        CountBucketView critical
) {
}
