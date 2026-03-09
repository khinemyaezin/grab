package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record RemoveZoneCommand(
        Id locationId,
        Id zoneId,
        Id removedBy
) implements Command<LocationResult> {
}
