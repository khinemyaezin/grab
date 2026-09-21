package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ReserveStockCommand;
import com.inventory.application.model.write.InventoryReservationResult;

public interface ReserveStockUseCase {
    InventoryReservationResult execute(ReserveStockCommand command);
}
