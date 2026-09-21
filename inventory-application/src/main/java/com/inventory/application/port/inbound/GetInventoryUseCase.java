package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;

public interface GetInventoryUseCase {
    GetInventoryResult execute(GetInventoryQuery query);
}
