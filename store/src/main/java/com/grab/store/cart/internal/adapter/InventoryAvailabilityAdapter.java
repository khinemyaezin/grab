package com.grab.store.cart.internal.adapter;

import com.cart.application.port.outbound.InventoryAvailabilityPort;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryAvailabilityAdapter implements InventoryAvailabilityPort {
    private final InventoryAvailabilityQueryPort inventoryAvailabilityQueryPort;

    @Override
    public Availability available(String sku, String salesChannelId) {
        InventoryAvailabilityQueryPort.Availability availability =
                inventoryAvailabilityQueryPort.available(sku, salesChannelId);
        return new Availability(availability.availableQty(), availability.untracked());
    }
}
