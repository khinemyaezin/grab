package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record ReserveStockCommand(
        Id inventoryItemId,
        int quantity,
        Id orderId,
        Id orderLineId,
        LocalDateTime expiresAt,
        String idempotencyKey,
        Id createdBy
) implements Command<InventoryReservationResult> {
}
