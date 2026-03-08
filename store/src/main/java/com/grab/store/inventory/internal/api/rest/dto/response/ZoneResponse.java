package com.grab.store.inventory.internal.api.rest.dto.response;

import java.util.List;

public record ZoneResponse(
        String id,
        String code,
        String name,
        String type,
        boolean active,
        List<BinResponse> bins
) {
}
