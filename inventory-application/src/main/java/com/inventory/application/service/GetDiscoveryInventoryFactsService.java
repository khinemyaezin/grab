package com.inventory.application.service;

import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;
import com.inventory.application.port.inbound.GetDiscoveryInventoryFactsUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetDiscoveryInventoryFactsService implements GetDiscoveryInventoryFactsUseCase {
    private final InventoryQueryPort inventoryQueryPort;

    @Override
    public GetDiscoveryInventoryFactsResult execute(GetDiscoveryInventoryFactsQuery query) {
        InventoryQueryPort.DiscoveryAvailability availability = inventoryQueryPort.discoveryAvailability(
                query.merchantId(),
                query.sku(),
                query.salesChannelId()
        );
        return new GetDiscoveryInventoryFactsResult(availability.availableQty(), availability.allowBackorder());
    }
}
