package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;

public record AddZoneCommand(
        Id locationId,
        String code,
        String name,
        ZoneType type,
        Boolean active,
        Id createdBy
) implements Command<LocationResult> {
}
