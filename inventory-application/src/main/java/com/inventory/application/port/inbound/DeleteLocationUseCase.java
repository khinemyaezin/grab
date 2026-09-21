package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeleteLocationCommand;

public interface DeleteLocationUseCase {
    Void execute(DeleteLocationCommand command);
}
