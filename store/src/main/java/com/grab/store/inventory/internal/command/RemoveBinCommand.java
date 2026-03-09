package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record RemoveBinCommand(
        Id locationId,
        Id zoneId,
        Id binId,
        Id removedBy
) implements Command<LocationResult> {
}
