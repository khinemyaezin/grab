package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.Set;

public record CreateAccessInvitationCommand(
        String inviteeEmail,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        Instant expiresAt,
        Id invitedBy,
        String actorScopeKey,
        String actorScopeId,
        Set<String> actorRoleCodes
) implements Command<AccessInvitationResult> {
}
