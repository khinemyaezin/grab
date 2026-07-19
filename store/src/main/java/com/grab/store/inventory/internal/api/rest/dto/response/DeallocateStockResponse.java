package com.grab.store.inventory.internal.api.rest.dto.response;

public record DeallocateStockResponse(
        String sku,
        String orderId,
        int requestedQuantity,
        int releasedQuantity
) {
}
