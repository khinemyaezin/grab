package com.inventory.application.port.inbound;

import com.inventory.application.model.write.MarkDamagedCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface MarkDamagedUseCase {
    InventoryItemResult execute(MarkDamagedCommand command);
}
