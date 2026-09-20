package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;

public interface DeactivateZoneUseCase {
    ZoneResult execute(DeactivateZoneCommand command);
}
