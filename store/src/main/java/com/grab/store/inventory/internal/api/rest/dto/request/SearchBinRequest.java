package com.grab.store.inventory.internal.api.rest.dto.request;

public record SearchBinRequest(
        String zoneId,
        String query,
        Boolean active
) {
}
