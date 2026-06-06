package com.grab.store.inventory.internal.command;

public record BinResult(
        String id,
        String zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
