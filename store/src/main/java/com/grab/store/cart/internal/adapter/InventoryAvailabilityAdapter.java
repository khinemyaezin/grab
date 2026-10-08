package com.grab.store.cart.internal.adapter;

import com.cart.application.port.outbound.InventoryAvailabilityPort;
import com.grab.store.inventory.port.InventoryAvailabilityQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryAvailabilityAdapter implements InventoryAvailabilityPort {
    private final InventoryAvailabilityQuery inventoryAvailabilityQuery;

    @Override
    public Availability available(String sku, String salesChannelId) {
        InventoryAvailabilityQuery.Availability availability =
                inventoryAvailabilityQuery.available(sku, salesChannelId);
        return new Availability(availability.availableQty(), availability.untracked());
    }
}
