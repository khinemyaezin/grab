package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ShipReservationCommand;
import com.inventory.application.model.write.InventoryReservationResult;

public interface ShipReservationUseCase {
    InventoryReservationResult execute(ShipReservationCommand command);
}
