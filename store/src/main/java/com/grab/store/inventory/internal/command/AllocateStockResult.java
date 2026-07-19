package com.grab.store.inventory.internal.command;

import java.util.List;

public record AllocateStockResult(
        boolean success,
        String sku,
        int requestedQuantity,
        int allocatedQuantity,
        String orderId,
        List<AllocationLineResult> allocations,
        String errorCode,
        String errorMessage
) {
    public record AllocationLineResult(
            String reservationId,
            String inventoryItemId,
            String locationId,
            int quantity
    ) {
    }
}
