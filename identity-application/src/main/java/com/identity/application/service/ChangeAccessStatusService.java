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
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;
import com.identity.domain.port.outbound.SecurityCatalogRepository;

@RequiredArgsConstructor
public class ChangeAccessStatusService implements ChangeAccessStatusUseCase {
    private final SecurityCatalogRepository catalogs;
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final RoleDelegationPolicy delegationPolicy;
    private final UserRepository users;
    public AccessAssignmentResult execute(ChangeAccessStatusCommand command) {
        AccessAssignment assignment = assignments.findById(command.assignmentId()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.AccessAssignmentNotFound(command.assignmentId().getValue()),
                        "Access assignment not found"
                )
        );
        AccessAssignmentStatus previousStatus = assignment.getStatus();
        var userLookup = users.findByIdForUpdate(assignment.getUserId());
        if (userLookup.isEmpty()) {
            userLookup = users.findById(assignment.getUserId());
        }
        var user = userLookup.orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(assignment.getUserId().getValue()),
                "User not found"
        ));
        var catalog = catalogs.loadForUpdate();
        var hierarchy = catalog.hierarchy();
        AccessScope actorScope = AccessScope.from(command.actorScopeKey(), command.actorScopeId());
        actorScope.requireEncompasses(assignment.getScope(), hierarchy);
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), assignment.getRoleCode());
        assignment.changeStatus(command.requestedStatus(), command.actorId());
        AccessAssignment saved = assignments.save(assignment);
        if (saved.getStatus() != AccessAssignmentStatus.ACTIVE) {
            sessions.revokeByAssignment(saved.getId().getValue());
        } else if (previousStatus != AccessAssignmentStatus.ACTIVE
                && RoleAdministrationPolicy.requiresSessionRevocation(saved.getRoleCode())) {
            user.invalidateAuthenticationSessions();
            users.save(user);
            var userId = user.getId();
            String userIdValue = userId.getValue();
            sessions.revokeAll(userIdValue);
        }
        return AccessAssignmentResult.from(saved);
    }
}
