package com.inventory.application.model.read;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record GetInventoryMovementsResult(
        Id id,
        Id inventoryItemId,
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
