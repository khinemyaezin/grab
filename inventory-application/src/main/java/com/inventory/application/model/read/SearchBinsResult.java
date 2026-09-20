package com.inventory.application.model.read;

import com.grab.framework.id.Id;

public record SearchBinsResult(
        Id id,
        Id zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
