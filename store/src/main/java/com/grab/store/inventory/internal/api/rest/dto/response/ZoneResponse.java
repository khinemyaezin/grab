package com.grab.store.inventory.internal.api.rest.dto.response;

public record ZoneResponse(
        String id,
        String locationId,
        String code,
        String name,
        String type,
        boolean active
) {
}
