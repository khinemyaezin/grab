package com.inventory.application.port.inbound;

import com.inventory.application.model.write.UpdateZoneCommand;
import com.inventory.application.model.write.ZoneResult;

public interface UpdateZoneUseCase {
    ZoneResult execute(UpdateZoneCommand command);
}
