package com.inventory.application.port.inbound;

import com.inventory.application.model.write.AnnounceInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface AnnounceInTransitUseCase {
    InventoryItemResult execute(AnnounceInTransitCommand command);
}
