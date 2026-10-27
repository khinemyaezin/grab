package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record DeallocateStockResponse(
        String sku,
        String orderId,
        int requestedQuantity,
        int releasedQuantity
) {
}
