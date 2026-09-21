package com.inventory.application.port.inbound;

import com.inventory.application.model.write.UpdateBinCommand;
import com.inventory.application.model.write.BinResult;

public interface UpdateBinUseCase {
    BinResult execute(UpdateBinCommand command);
}
