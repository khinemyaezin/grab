package com.grab.store.inventory.internal.query;

import java.time.LocalDateTime;
import java.util.List;

public record GetInventoryMovementsResult(
        String inventoryItemId,
        List<Movement> movements
) {
    public record Movement(
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
}
