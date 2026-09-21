package com.inventory.application.model.read;

public record InventoryStockHealthBreakdownView(
        long eligibleItems,
        CountBucketView inStock,
        CountBucketView lowStock,
        CountBucketView outOfStock,
        CountBucketView critical
) {
}
