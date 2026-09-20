package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ReturnToVendorCommand;
import com.inventory.application.model.write.InventoryItemResult;

public interface ReturnToVendorUseCase {
    InventoryItemResult execute(ReturnToVendorCommand command);
}
