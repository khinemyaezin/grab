package com.inventory.application.port.inbound;

import com.inventory.application.model.write.TransferInventoryCommand;
import com.inventory.application.model.write.TransferInventoryResult;

public interface TransferInventoryUseCase {
    TransferInventoryResult execute(TransferInventoryCommand command);
}
