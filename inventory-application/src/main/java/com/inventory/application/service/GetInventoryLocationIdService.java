package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetInventoryLocationIdUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.model.read.GetInventoryLocationIdQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetInventoryLocationIdService implements GetInventoryLocationIdUseCase {

    private final InventoryQueryPort inventoryQueryPort;

    public String execute(GetInventoryLocationIdQuery query) {
        return inventoryQueryPort.findById(query.inventoryItemId().getValue())
                .map(item -> item.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(query.inventoryItemId().getValue())));
    }

    public Class<GetInventoryLocationIdQuery> getQueryType() {
        return GetInventoryLocationIdQuery.class;
    }
}
