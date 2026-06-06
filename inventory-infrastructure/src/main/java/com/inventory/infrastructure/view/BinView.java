package com.inventory.infrastructure.view;

public record BinView(
        String uuid,
        String code,
        String name,
        Integer maxCapacity,
        boolean active,
        String zoneId
) {
}
