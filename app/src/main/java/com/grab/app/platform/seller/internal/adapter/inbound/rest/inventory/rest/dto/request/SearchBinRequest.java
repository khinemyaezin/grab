package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

public record SearchBinRequest(
        String zoneId,
        String query,
        Boolean active
) {
}
