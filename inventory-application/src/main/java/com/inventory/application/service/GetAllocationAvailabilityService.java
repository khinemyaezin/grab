package com.inventory.application.service;

import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAllocationAvailabilityService implements GetAllocationAvailabilityUseCase {

    private final InventoryQueryPort inventoryQueryPort;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;

    public GetAllocationAvailabilityResult execute(GetAllocationAvailabilityQuery query) {
        int requested = query.quantity() == null ? 0 : query.quantity();
        if (isUntracked(query.sku())) {
            int available = requested > 0 ? requested : 1;
            return new GetAllocationAvailabilityResult(query.sku(), available, true, requested, true);
        }
        int available = inventoryQueryPort.sumAvailableForAllocation(
                query.sku(),
                query.salesChannelId()
        );
        boolean canAllocate = query.quantity() == null ? available > 0 : available >= query.quantity();
        return new GetAllocationAvailabilityResult(query.sku(), available, canAllocate, requested, false);
    }

    public Class<GetAllocationAvailabilityQuery> getQueryType() {
        return GetAllocationAvailabilityQuery.class;
    }

    private boolean isUntracked(String sku) {
        return productVariantViewQueryPort.findActiveBySku(sku)
                .map(view -> !view.isManageInventory())
                .orElse(false);
    }
}
