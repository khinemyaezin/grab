package com.inventory.application.model.read;

public record GetAllocationAvailabilityResult(
        String sku,
        int availableQuantity,
        boolean canAllocate,
        int requestedQuantity,
        boolean untracked
) {
}
