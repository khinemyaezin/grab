package com.grab.store.inventory.internal.query;

public record GetAllocationAvailabilityResult(
        String sku,
        int availableQuantity,
        boolean canAllocate,
        int requestedQuantity
) {
}
