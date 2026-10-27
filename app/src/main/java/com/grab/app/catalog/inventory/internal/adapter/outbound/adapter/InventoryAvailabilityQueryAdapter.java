package com.grab.app.catalog.inventory.internal.adapter.outbound;

import com.grab.app.catalog.inventory.internal.adapter.outbound.mapper.InventoryAvailabilityQueryMapper;
import com.grab.app.catalog.inventory.internal.config.InventoryReadTransactional;
import com.grab.app.catalog.inventory.port.InventoryAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.ListSkusAtLocationQuery;
import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryAvailabilityQueryAdapter implements InventoryAvailabilityQuery {

    private final GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase;
    private final ListSkusAtLocationUseCase listSkusAtLocationUseCase;
    private final InventoryAvailabilityQueryMapper mapper;

    @Override
    @InventoryReadTransactional
    public Availability available(String sku, String salesChannelId) {
        var result = getAllocationAvailabilityUseCase.execute(
                new GetAllocationAvailabilityQuery(sku, null, salesChannelId));
        return mapper.toAvailability(result);
    }

    @Override
    @InventoryReadTransactional
    public List<String> skusAtLocation(String locationId) {
        return listSkusAtLocationUseCase.execute(new ListSkusAtLocationQuery(locationId));
    }
}
