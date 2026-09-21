package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeactivateLocationCommand;
import com.inventory.application.model.write.LocationResult;

public interface DeactivateLocationUseCase {
    LocationResult execute(DeactivateLocationCommand command);
}
