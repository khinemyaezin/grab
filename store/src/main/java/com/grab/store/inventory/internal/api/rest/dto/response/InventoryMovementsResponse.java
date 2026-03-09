package com.grab.store.inventory.internal.api.rest.dto.response;

import java.util.List;

public record InventoryMovementsResponse(
        String inventoryItemId,
        List<StockMovementResponse> movements
) {
}
