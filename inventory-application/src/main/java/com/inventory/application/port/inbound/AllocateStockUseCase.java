package com.inventory.application.port.inbound;

import com.inventory.application.model.write.AllocateStockCommand;
import com.inventory.application.model.write.AllocateStockResult;

public interface AllocateStockUseCase {
    AllocateStockResult execute(AllocateStockCommand command);
}
