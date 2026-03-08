package com.grab.store.inventory.internal.api.rest.dto.response;

public record BinResponse(
        String id,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
