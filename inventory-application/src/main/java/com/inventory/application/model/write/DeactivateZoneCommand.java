package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeactivateZoneCommand(
        Id zoneId,
        String actorId,
        String scopeKey,
        String scopeId
) implements Command<ZoneResult> {
}
