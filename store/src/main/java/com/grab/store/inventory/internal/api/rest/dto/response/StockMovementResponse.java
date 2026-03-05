package com.grab.store.inventory.internal.api.rest.dto.response;

import java.time.LocalDateTime;

public record StockMovementResponse(
        String id,
        String inventoryItemId,
        String type,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        int onHandBefore,
        int onHandAfter,
        int reservedBefore,
        int reservedAfter,
        String referenceId,
        LocalDateTime createdAt
) {
}
