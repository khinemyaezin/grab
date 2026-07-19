package com.grab.store.inventory.internal.api.rest.dto.response;

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
