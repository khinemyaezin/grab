package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record RevokeSessionsByScopeCommand(
        String platformCode,
        String scopeKey,
        String scopeId
) implements Command<Void> {
}
