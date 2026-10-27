package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record BinResponse(
        String id,
        String zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
