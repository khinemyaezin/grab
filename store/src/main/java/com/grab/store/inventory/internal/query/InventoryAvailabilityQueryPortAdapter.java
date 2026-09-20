package com.grab.store.inventory.internal.query;

import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryAvailabilityQueryPortAdapter implements InventoryAvailabilityQueryPort {
    private final InventoryQueryPort inventoryQueryPort;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;

    @Override
    @InventoryReadTransactional
    public Availability available(String sku, String salesChannelId) {
        boolean untracked = productVariantViewQueryPort.findActiveBySku(sku)
                .map(view -> !view.isManageInventory())
                .orElse(false);
        if (untracked) {
            return new Availability(0, true);
        }
        int qty = inventoryQueryPort.sumAvailableForAllocation(sku, salesChannelId);
        return new Availability(qty, false);
    }

    @Override
    @InventoryReadTransactional
    public List<String> skusAtLocation(String locationId) {
        return inventoryQueryPort.findSkusByLocation(locationId);
    }
}
