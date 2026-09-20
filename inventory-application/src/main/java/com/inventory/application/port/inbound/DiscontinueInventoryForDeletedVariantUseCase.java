package com.inventory.application.port.inbound;

import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantCommand;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantResult;

public interface DiscontinueInventoryForDeletedVariantUseCase {
    DiscontinueInventoryForDeletedVariantResult execute(DiscontinueInventoryForDeletedVariantCommand command);
}
