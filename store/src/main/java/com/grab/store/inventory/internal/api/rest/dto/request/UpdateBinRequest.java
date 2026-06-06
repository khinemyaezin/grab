package com.grab.store.inventory.internal.api.rest.dto.request;

public record UpdateBinRequest(
        String code,
        String name,
        Integer maxCapacity,
        Boolean active
) {
}
