package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record AddBinCommand(
        Id locationId,
        Id zoneId,
        String code,
        String name,
        Integer maxCapacity,
        Boolean active,
        Id createdBy
) implements Command<LocationResult> {
}
