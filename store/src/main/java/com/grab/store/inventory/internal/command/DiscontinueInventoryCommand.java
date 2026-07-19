package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DiscontinueInventoryCommand(
        Id inventoryItemId,
        Id createdBy,
        String scopeKey,
        String scopeId
) implements Command<InventoryItemResult> {
}
