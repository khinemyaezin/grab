package com.grab.store.inventory.queries;

public record SkuAvailabilityResult(
        String sku,
        int availableQuantity,
        boolean inStock
) {
}
