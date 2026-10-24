package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;

public interface GetDiscoveryInventoryFactsUseCase {
    GetDiscoveryInventoryFactsResult execute(GetDiscoveryInventoryFactsQuery query);
}
