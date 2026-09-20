package com.inventory.application.model.write;

public record BinResult(
        String id,
        String zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
