package com.inventory.infrastructure.view;

public record InventoryStatusBreakdownView(
        CountBucketView active,
        CountBucketView outOfStock,
        CountBucketView suspended,
        CountBucketView discontinued
) {
}
