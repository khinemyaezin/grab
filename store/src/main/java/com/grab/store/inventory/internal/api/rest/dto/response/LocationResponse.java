package com.grab.store.inventory.internal.api.rest.dto.response;

import java.util.List;

public record LocationResponse(
        String id,
        String code,
        String name,
        String type,
        boolean active,
        LocationAddressResponse address,
        List<ZoneResponse> zones
) {
}
