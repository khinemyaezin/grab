package com.identity.application.service;

import com.identity.application.port.inbound.ChangeAccessStatusUseCase;

import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeAccessStatusService implements ChangeAccessStatusUseCase {
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final RoleDelegationPolicy delegationPolicy;
    public AccessAssignmentResult execute(ChangeAccessStatusCommand command) {
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
}
