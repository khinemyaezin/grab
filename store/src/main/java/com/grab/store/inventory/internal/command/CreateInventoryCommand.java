package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record CreateInventoryCommand(
        String sku,
        Id merchantId,
        Id locationId,
        int initialQuantity,
        Integer safetyStock,
        Integer reorderPoint,
        Integer reorderQuantity,
        Integer maxStock,
        Id createdBy,
        String scopeKey,
        String scopeId
) implements Command<InventoryItemResult> {
}
