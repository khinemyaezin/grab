package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeactivateZoneCommand(
        Id zoneId,
        String actorId
) implements Command<ZoneResult> {
}
