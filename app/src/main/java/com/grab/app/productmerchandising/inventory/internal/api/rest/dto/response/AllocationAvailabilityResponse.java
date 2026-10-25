package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.response;

public record AllocationAvailabilityResponse(
        String sku,
        int availableQuantity,
        boolean canAllocate,
        int requestedQuantity
) {
}
