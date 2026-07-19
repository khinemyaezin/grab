package com.grab.store.inventory.internal.command;

public record DeallocateStockResult(
        String sku,
        String orderId,
        int requestedQuantity,
        int releasedQuantity
) {
}
