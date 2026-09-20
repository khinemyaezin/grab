package com.inventory.application.port.inbound;

import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.LocationResult;

public interface CreateLocationUseCase {
    LocationResult execute(CreateLocationCommand command);
}
