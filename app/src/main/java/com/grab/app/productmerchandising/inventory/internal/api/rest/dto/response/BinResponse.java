package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.response;

public record BinResponse(
        String id,
        String zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
