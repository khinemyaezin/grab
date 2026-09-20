package com.inventory.application.port.inbound;

import com.inventory.application.model.write.UpdateLocationCommand;
import com.inventory.application.model.write.LocationResult;

public interface UpdateLocationUseCase {
    LocationResult execute(UpdateLocationCommand command);
}
