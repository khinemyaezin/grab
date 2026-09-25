package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.Set;

public record ReplaceAccessCommand(
        Id userId,
        String previousRoleCode,
        String replacementRoleCode,
        String scopeKey,
        String scopeId,
        Set<String> authorityCodes
) implements Command<AccessAssignmentResult> {

    public ReplaceAccessCommand(
            Id userId,
            String previousRoleCode,
            String replacementRoleCode,
            String scopeKey,
            String scopeId
    ) {
        this(userId, previousRoleCode, replacementRoleCode, scopeKey, scopeId, Set.of());
    }

    public ReplaceAccessCommand(
            Id userId,
            String replacementRoleCode,
            String scopeKey,
            String scopeId
    ) {
        this(userId, null, replacementRoleCode, scopeKey, scopeId, Set.of());
    }
}
