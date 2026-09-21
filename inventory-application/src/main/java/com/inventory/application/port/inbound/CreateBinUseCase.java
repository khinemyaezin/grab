package com.inventory.application.port.inbound;

import com.inventory.application.model.write.CreateBinCommand;
import com.inventory.application.model.write.BinResult;

public interface CreateBinUseCase {
    BinResult execute(CreateBinCommand command);
}
