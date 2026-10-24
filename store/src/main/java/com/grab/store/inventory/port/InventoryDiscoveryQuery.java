package com.grab.store.inventory.port;

import com.grab.framework.id.Id;

import java.util.List;

public interface InventoryDiscoveryQuery {
    DiscoveryAvailability discoveryAvailability(Id merchantId, String sku, Id salesChannelId);

    List<String> skusAtLocation(String locationId);

    record DiscoveryAvailability(int availableQty, boolean allowBackorder) {
    }
}
