package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ReceiveInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface ReceiveInTransitUseCase {
    InventoryItemResult execute(ReceiveInTransitCommand command);
}
