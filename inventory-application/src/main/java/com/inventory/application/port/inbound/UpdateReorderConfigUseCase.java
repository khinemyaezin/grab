package com.inventory.application.port.inbound;

import com.inventory.application.model.write.UpdateReorderConfigCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface UpdateReorderConfigUseCase {
    InventoryItemResult execute(UpdateReorderConfigCommand command);
}
