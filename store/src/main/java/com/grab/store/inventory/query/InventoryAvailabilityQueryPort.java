package com.grab.store.inventory.query;

import java.util.List;

public interface InventoryAvailabilityQueryPort {
    Availability available(String sku, String salesChannelId);

    List<String> skusAtLocation(String locationId);

    record Availability(int availableQty, boolean untracked) {
    }
}
