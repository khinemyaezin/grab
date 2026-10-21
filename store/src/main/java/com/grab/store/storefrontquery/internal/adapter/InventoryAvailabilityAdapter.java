package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.framework.id.Id;
import com.grab.store.inventory.port.InventoryAvailabilityQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("storeFrontQueryInventoryAvailabilityAdapter")
@RequiredArgsConstructor
public class InventoryAvailabilityAdapter implements InventoryPort {
    private final InventoryAvailabilityQuery inventory;

    @Override
    public Availability available(String sku, Id salesChannelId) {
        String channelValue = salesChannelId.getValue();
        InventoryAvailabilityQuery.Availability availability = inventory.available(sku, channelValue);
        int availableQty = availability.availableQty();
        boolean untracked = availability.untracked();
        return new Availability(availableQty, untracked);
    }

    @Override
    public List<String> skusAtLocation(Id locationId) {
        String locationValue = locationId.getValue();
        return inventory.skusAtLocation(locationValue);
    }
}
