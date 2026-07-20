package com.inventory.infrastructure.view;

public record InventoryQuantityTotalsView(
        long onHand,
        long reserved,
        long inTransit,
        long damaged,
        long available
) {
}
