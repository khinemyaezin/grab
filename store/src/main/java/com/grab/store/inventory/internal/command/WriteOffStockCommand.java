package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record WriteOffStockCommand(
        Id inventoryItemId,
        int quantity,
        String reason,
        String notes,
        Id createdBy,
        String scopeKey,
        String scopeId
) implements Command<InventoryItemResult> {
}
