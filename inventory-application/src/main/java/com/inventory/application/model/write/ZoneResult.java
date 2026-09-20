package com.inventory.application.model.write;

public record ZoneResult(
        String id,
        String locationId,
        String code,
        String name,
        String type,
        boolean active
) {
}
