package com.inventory.infrastructure.specification.jpa;

public record BinSearchCriteria(
        String merchantId,
        String zoneId,
        String query,
        Boolean active
) {
}
