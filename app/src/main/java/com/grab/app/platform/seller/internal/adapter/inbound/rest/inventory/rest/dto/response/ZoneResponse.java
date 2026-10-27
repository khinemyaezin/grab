package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record ZoneResponse(
        String id,
        String locationId,
        String code,
        String name,
        String type,
        boolean active
) {
}
