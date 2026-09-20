package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ActivateBinCommand;
import com.inventory.application.model.write.BinResult;

public interface ActivateBinUseCase {
    BinResult execute(ActivateBinCommand command);
}
