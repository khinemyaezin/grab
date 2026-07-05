package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ReplaceAccessCommand(
        Id userId,
        String platformCode,
        String replacementRoleCode,
        String scopeKey,
        String scopeId
) implements Command<AccessAssignmentResult> {
}
