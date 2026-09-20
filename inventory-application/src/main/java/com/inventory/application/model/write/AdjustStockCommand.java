package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.AdjustmentReason;

public record AdjustStockCommand(
        Id inventoryItemId,
        int newOnHandQuantity,
        AdjustmentReason reason,
        Id createdBy
,
        String scopeKey,
        String scopeId
) implements Command<InventoryItemResult> {
}
