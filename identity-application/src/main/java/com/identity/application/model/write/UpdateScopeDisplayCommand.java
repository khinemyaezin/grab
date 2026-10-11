package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateScopeDisplayCommand(
        Id scopeId,
        String name
) implements Command<Void> {
}
