package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

public interface InventoryPort {
    Availability available(String sku, Id merchantId, Id salesChannelId);

    record Availability(int availableQty, boolean allowBackorder) {
    }
}
