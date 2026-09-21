package com.inventory.application.service;

import com.inventory.application.model.read.ListSkusAtLocationQuery;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListSkusAtLocationService implements ListSkusAtLocationUseCase {

    private final InventoryQueryPort inventoryQueryPort;

    @Override
    public List<String> execute(ListSkusAtLocationQuery query) {
        return inventoryQueryPort.findSkusByLocation(query.locationId());
    }
}
