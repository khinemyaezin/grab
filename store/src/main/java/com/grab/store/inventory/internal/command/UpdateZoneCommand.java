package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;

public record UpdateZoneCommand(
        Id zoneId,
        String code,
        String name,
        ZoneType type,
        Boolean active,
        String actorId,
        String scopeKey,
        String scopeId
) implements Command<ZoneResult> {
}
