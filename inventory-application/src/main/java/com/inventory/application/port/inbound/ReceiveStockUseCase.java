package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ReceiveStockCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface ReceiveStockUseCase {
    InventoryItemResult execute(ReceiveStockCommand command);
}
