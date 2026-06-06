package com.grab.store.inventory.internal.query;

import com.grab.framework.id.Id;

public record GetBinResult(
        Id id,
        Id zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {
}
