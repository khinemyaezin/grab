package com.grab.store.inventory.port;

import com.grab.framework.id.Id;

public interface InventoryDiscoveryQuery {
    DiscoveryAvailability discoveryAvailability(Id merchantId, String sku, Id salesChannelId);

    record DiscoveryAvailability(int availableQty, boolean allowBackorder) {
    }
}
