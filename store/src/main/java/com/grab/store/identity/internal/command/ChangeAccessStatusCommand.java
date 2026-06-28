package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.identity.domain.enums.AccessAssignmentStatus;

import java.util.Set;

public record ChangeAccessStatusCommand(
        Id assignmentId,
        AccessAssignmentStatus requestedStatus,
        String actorScopeType,
        String actorScopeId,
        Id actorId,
        Set<String> actorRoleCodes
) implements Command<AccessAssignmentResult> {
}
