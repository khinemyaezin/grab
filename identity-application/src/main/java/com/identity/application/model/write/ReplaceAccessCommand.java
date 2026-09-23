package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ReplaceAccessCommand(
        Id userId,
        String platformCode,
        String previousRoleCode,
        String replacementRoleCode,
        String scopeKey,
        String scopeId
) implements Command<AccessAssignmentResult> {

    public ReplaceAccessCommand(
            Id userId,
            String platformCode,
            String replacementRoleCode,
            String scopeKey,
            String scopeId
    ) {
        this(userId, platformCode, null, replacementRoleCode, scopeKey, scopeId);
    }
}
