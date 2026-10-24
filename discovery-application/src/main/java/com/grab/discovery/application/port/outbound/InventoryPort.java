package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.List;

public interface InventoryPort {
    Availability available(String sku, Id merchantId, Id salesChannelId);

    List<String> skusAtLocation(Id locationId);

    record Availability(int availableQty, boolean allowBackorder) {
    }
}
