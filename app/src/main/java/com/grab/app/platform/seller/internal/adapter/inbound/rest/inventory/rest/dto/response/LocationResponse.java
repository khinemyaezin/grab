package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record LocationResponse(
        String id,
        String code,
        String name,
        String type,
        boolean active,
        LocationAddressResponse address
) {
}
