package com.inventory.application.port.inbound;

import com.inventory.application.model.write.AdjustStockCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface AdjustStockUseCase {
    InventoryItemResult execute(AdjustStockCommand command);
}
