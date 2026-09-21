package com.inventory.application.model.write;

public record DeallocateStockResult(
        String sku,
        String orderId,
        int requestedQuantity,
        int releasedQuantity
) {
}
