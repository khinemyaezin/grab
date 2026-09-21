package com.inventory.application.port.inbound;

import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.ZoneResult;

public interface CreateZoneUseCase {
    ZoneResult execute(CreateZoneCommand command);
}
