package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateScopeDisplayCommand(
        String scopeKey,
        Id scopeId,
        String title,
        String subtitle,
        long sourceRevision
) implements Command<Void> {
}
