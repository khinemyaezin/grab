package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record TransferInventoryCommand(
        Id inventoryItemId,
        Id toLocationId,
        int quantity,
        String notes,
        Id createdBy,
        String scopeKey,
        String scopeId
) implements Command<TransferInventoryResult> {
}
