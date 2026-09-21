package com.inventory.application.port.inbound;

import com.inventory.application.model.write.WriteOffStockCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface WriteOffStockUseCase {
    InventoryItemResult execute(WriteOffStockCommand command);
}
