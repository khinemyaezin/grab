package com.grab.store.inventory.port;

import java.util.List;

public interface InventoryAvailabilityQuery {
    Availability available(String sku, String salesChannelId);

    List<String> skusAtLocation(String locationId);

    record Availability(int availableQty, boolean untracked) {
    }
}
