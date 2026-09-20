package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ActivateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface ActivateInventoryUseCase {
    InventoryItemResult execute(ActivateInventoryCommand command);
}
