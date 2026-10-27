package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

import java.util.List;

public record AllocateStockResponse(
        boolean success,
        String sku,
        int requestedQuantity,
        int allocatedQuantity,
        String orderId,
        List<AllocationLineResponse> allocations
) {
    public record AllocationLineResponse(
            String reservationId,
            String inventoryItemId,
            String locationId,
            int quantity
    ) {
    }
}
