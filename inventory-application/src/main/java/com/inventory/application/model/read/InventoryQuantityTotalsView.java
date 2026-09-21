package com.inventory.application.model.read;

public record InventoryQuantityTotalsView(
        long onHand,
        long reserved,
        long inTransit,
        long damaged,
        long available
) {
}
