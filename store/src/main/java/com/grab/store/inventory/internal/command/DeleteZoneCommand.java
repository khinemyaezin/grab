package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeleteZoneCommand(
        Id zoneId,
        String actorId,
        String scopeKey,
        String scopeId
) implements Command<Void> {
}
