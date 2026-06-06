package com.grab.store.inventory.internal.command;

public record ZoneResult(
        String id,
        String locationId,
        String code,
        String name,
        String type,
        boolean active
) {
}
