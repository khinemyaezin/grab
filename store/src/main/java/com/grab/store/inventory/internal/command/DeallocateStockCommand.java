package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeallocateStockCommand(
        String sku,
        int quantity,
        String orderId,
        Id createdBy,
        String scopeKey,
        String scopeId
) implements Command<DeallocateStockResult> {
}
