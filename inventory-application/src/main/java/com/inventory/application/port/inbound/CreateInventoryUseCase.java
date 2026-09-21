package com.inventory.application.port.inbound;

import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface CreateInventoryUseCase {
    InventoryItemResult execute(CreateInventoryCommand command);
}
