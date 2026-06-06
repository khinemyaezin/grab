package com.grab.store.inventory.internal.api.rest.dto.response;

public record LocationResponse(
        String id,
        String code,
        String name,
        String type,
        boolean active,
        LocationAddressResponse address
) {
}
