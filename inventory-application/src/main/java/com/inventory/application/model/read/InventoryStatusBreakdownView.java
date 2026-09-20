package com.inventory.application.model.read;

public record InventoryStatusBreakdownView(
        CountBucketView active,
        CountBucketView outOfStock,
        CountBucketView suspended,
        CountBucketView discontinued
) {
}
