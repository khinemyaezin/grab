package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record AllocateStockCommand(
        String sku,
        int quantity,
        String orderId,
        String orderLineId,
        Id locationId,
        LocalDateTime expiresAt,
        Id createdBy,
        String scopeKey,
        String scopeId
) implements Command<AllocateStockResult> {
}
