package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateBinCommand(
        Id locationId,
        Id zoneId,
        Id binId,
        String code,
        String name,
        Integer maxCapacity,
        Boolean active,
        Id updatedBy
) implements Command<LocationResult> {
}
