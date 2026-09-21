package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetInventoryLocationIdQuery;

public interface GetInventoryLocationIdUseCase {
    String execute(GetInventoryLocationIdQuery query);
}
