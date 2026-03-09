package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeactivateLocationCommand(
        Id locationId,
        Id initiatedBy
) implements Command<LocationResult> {
}
