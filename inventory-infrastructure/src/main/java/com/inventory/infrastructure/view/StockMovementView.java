package com.inventory.infrastructure.view;

import com.inventory.domain.enums.StockMovementType;

import java.time.LocalDateTime;

public record StockMovementView(
        String uuid,
        String inventoryItemUuid,
        StockMovementType type,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        int onHandBefore,
        int onHandAfter,
        int reservedBefore,
        int reservedAfter,
        String referenceId,
        LocalDateTime createdAt,
        String createdBy
) {
}
