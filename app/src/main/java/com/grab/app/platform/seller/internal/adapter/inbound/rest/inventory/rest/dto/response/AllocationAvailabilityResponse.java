package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record AllocationAvailabilityResponse(
        String sku,
        int availableQuantity,
        boolean canAllocate,
        int requestedQuantity
) {
}
