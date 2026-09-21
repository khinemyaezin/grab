package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeactivateBinCommand;
import com.inventory.application.model.write.BinResult;

public interface DeactivateBinUseCase {
    BinResult execute(DeactivateBinCommand command);
}
