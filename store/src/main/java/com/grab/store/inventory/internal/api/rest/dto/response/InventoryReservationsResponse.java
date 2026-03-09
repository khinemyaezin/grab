package com.grab.store.inventory.internal.api.rest.dto.response;

import java.util.List;

public record InventoryReservationsResponse(
        String inventoryItemId,
        List<InventoryReservationResponse> reservations
) {
}
