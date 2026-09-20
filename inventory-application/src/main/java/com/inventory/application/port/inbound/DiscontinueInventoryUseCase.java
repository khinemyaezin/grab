package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DiscontinueInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface DiscontinueInventoryUseCase {
    InventoryItemResult execute(DiscontinueInventoryCommand command);
}
