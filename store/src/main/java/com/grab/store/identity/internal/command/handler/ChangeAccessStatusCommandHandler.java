package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.ChangeAccessStatusCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeAccessStatusCommandHandler
        implements CommandHandler<ChangeAccessStatusCommand, AccessAssignmentResult> {
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final RoleDelegationPolicy delegationPolicy;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(ChangeAccessStatusCommand command) {
        AccessAssignment assignment = assignments.findById(command.assignmentId()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.AccessAssignmentNotFound(command.assignmentId().getValue()),
                        "Access assignment not found"
                )
        );
        AccessScope.from(command.actorScopeKey(), command.actorScopeId())
                .requireEncompasses(assignment.getScope());
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), assignment.getRoleCode());
        assignment.changeStatus(command.requestedStatus(), command.actorId());
        AccessAssignment saved = assignments.save(assignment);
        if (saved.getStatus() != AccessAssignmentStatus.ACTIVE) {
            sessions.revokeByAssignment(saved.getId().getValue());
        }
        return AccessAssignmentResult.from(saved);
    }

    @Override
    public Class<ChangeAccessStatusCommand> getCommandType() {
        return ChangeAccessStatusCommand.class;
    }
}
