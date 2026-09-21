package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeleteBinCommand;

public interface DeleteBinUseCase {
    Void execute(DeleteBinCommand command);
}
