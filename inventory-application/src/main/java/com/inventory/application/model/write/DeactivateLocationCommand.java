package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeactivateLocationCommand(
        Id locationId,
        Id initiatedBy
,
        String scopeKey,
        String scopeId
) implements Command<LocationResult> {
}
