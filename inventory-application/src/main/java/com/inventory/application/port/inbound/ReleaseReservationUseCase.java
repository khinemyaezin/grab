package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ReleaseReservationCommand;
import com.inventory.application.model.write.InventoryReservationResult;

public interface ReleaseReservationUseCase {
    InventoryReservationResult execute(ReleaseReservationCommand command);
}
