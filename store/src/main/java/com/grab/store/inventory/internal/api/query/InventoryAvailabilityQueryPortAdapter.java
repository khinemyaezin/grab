package com.grab.store.inventory.internal.api.query;

import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.ListSkusAtLocationQuery;
import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryAvailabilityQueryPortAdapter implements InventoryAvailabilityQueryPort {

    private final GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase;
    private final ListSkusAtLocationUseCase listSkusAtLocationUseCase;

    @Override
    @InventoryReadTransactional
    public Availability available(String sku, String salesChannelId) {
        var result = getAllocationAvailabilityUseCase.execute(
                new GetAllocationAvailabilityQuery(sku, null, salesChannelId));
        if (result.untracked()) {
            return new Availability(0, true);
        }
        return new Availability(result.availableQuantity(), false);
    }

    @Override
    @InventoryReadTransactional
    public List<String> skusAtLocation(String locationId) {
        return listSkusAtLocationUseCase.execute(new ListSkusAtLocationQuery(locationId));
    }
}
