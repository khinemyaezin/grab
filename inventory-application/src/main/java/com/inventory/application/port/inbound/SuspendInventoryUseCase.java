package com.inventory.application.port.inbound;

import com.inventory.application.model.write.SuspendInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface SuspendInventoryUseCase {
    InventoryItemResult execute(SuspendInventoryCommand command);
}
