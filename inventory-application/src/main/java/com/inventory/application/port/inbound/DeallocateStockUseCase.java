package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DeallocateStockCommand;
import com.inventory.application.model.write.DeallocateStockResult;

public interface DeallocateStockUseCase {
    DeallocateStockResult execute(DeallocateStockCommand command);
}
