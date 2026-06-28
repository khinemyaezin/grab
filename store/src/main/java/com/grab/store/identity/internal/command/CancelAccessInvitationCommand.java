package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.Set;

public record CancelAccessInvitationCommand(
        Id invitationId,
        String actorScopeKey,
        String actorScopeId,
        Set<String> actorRoleCodes
) implements Command<AccessInvitationResult> {
}
