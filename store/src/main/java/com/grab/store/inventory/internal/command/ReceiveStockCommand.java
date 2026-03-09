package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.StockMovementType;

public record ReceiveStockCommand(
        Id inventoryItemId,
        int quantity,
        StockMovementType type,
        String referenceId,
        Id createdBy
) implements Command<InventoryItemResult> {
}
