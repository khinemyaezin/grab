package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeleteZoneCommand;

public interface DeleteZoneUseCase {
    Void execute(DeleteZoneCommand command);
}
