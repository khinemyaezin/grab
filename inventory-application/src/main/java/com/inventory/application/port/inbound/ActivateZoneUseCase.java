package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ActivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;

public interface ActivateZoneUseCase {
    ZoneResult execute(ActivateZoneCommand command);
}
