package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ActivateLocationCommand;
import com.inventory.application.model.write.LocationResult;

public interface ActivateLocationUseCase {
    LocationResult execute(ActivateLocationCommand command);
}
