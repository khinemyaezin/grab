package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetAllocationAvailabilityQuery(
        String sku,
        Integer quantity,
        String salesChannelId
) implements Query<GetAllocationAvailabilityResult> {
    public GetAllocationAvailabilityQuery(String sku, Integer quantity) {
        this(sku, quantity, null);
    }
}
