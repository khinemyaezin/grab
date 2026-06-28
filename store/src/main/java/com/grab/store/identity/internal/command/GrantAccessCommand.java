package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.identity.domain.enums.AccessScopeType;

import java.time.Instant;
import java.util.Set;

public record GrantAccessCommand(
        Id userId,
        String platformCode,
        String roleCode,
        AccessScopeType scopeType,
        String scopeId,
        Instant expiresAt,
        Id assignedBy,
        String actorScopeType,
        String actorScopeId,
        Set<String> actorRoleCodes
) implements Command<AccessAssignmentResult> {
}
