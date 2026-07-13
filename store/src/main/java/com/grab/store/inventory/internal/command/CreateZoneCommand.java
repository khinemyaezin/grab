package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;

public record CreateZoneCommand(
        Id locationId,
        String code,
        String name,
        ZoneType type,
        String actorId,
        String scopeKey,
        String scopeId
) implements Command<ZoneResult> {
}
