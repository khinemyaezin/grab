package com.inventory.application.model.read;

public record BinView(
        String uuid,
        String code,
        String name,
        Integer maxCapacity,
        boolean active,
        String zoneId
) {
}
