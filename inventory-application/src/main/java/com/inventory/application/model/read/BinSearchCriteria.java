package com.inventory.application.model.read;

public record BinSearchCriteria(
        String merchantId,
        String zoneId,
        String query,
        Boolean active
) {
}
