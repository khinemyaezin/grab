package com.identity.application.service;

import com.identity.application.port.inbound.GrantAccessUseCase;

import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.port.outbound.ScopeOwnershipPort;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Role;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.port.outbound.SessionStore;

import java.time.Instant;

public class GrantAccessService implements GrantAccessUseCase {
    private final SecurityCatalogRepository catalogs;
    private final UserRepository users;
    private final RoleRepository roles;
    private final AccessAssignmentRepository assignments;
    private final RoleDelegationPolicy delegationPolicy;
    private final IdGenerator ids;
    private final ScopeOwnershipPort scopeOwnershipPort;
    private final SessionStore sessions;

    public GrantAccessService(UserRepository users, RoleRepository roles, AccessAssignmentRepository assignments,
            RoleDelegationPolicy delegationPolicy, IdGenerator ids, ScopeOwnershipPort scopeOwnershipPort,
            SecurityCatalogRepository catalogs, SessionStore sessions) {
        this.users = users;
        this.roles = roles;
        this.assignments = assignments;
        this.delegationPolicy = delegationPolicy;
        this.ids = ids;
        this.scopeOwnershipPort = scopeOwnershipPort;
        this.catalogs = catalogs;
        this.sessions = sessions;
    }

    public GrantAccessService(UserRepository users, RoleRepository roles, AccessAssignmentRepository assignments,
            RoleDelegationPolicy delegationPolicy, IdGenerator ids, ScopeOwnershipPort scopeOwnershipPort,
            SecurityCatalogRepository catalogs) {
        this(users, roles, assignments, delegationPolicy, ids, scopeOwnershipPort, catalogs, null);
    }

    @Override
    public AccessAssignmentResult execute(GrantAccessCommand command) {
        var userLookup = users.findByIdForUpdate(command.userId());
        if (userLookup.isEmpty()) {
            userLookup = users.findById(command.userId());
        }
        var user = userLookup.orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.userId().getValue()),
                "User not found"
        ));
        Role role = roles.findByCode(command.roleCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(command.roleCode()),
                        "Role not found"
                )
        );
        role.requireAssignable();
        AccessScope scope = AccessScope.from(command.scopeKey(), command.scopeId());
        AccessScope actorScope = AccessScope.from(command.actorScopeKey(), command.actorScopeId());
        var catalog = catalogs.loadForUpdate();
        var hierarchy = catalog.hierarchy();
        actorScope.requireEncompasses(scope, hierarchy);
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), command.roleCode());
        if (!actorScope.isGlobal() && !command.actorScopeKey().equals(command.scopeKey())) {
            boolean owned = scopeOwnershipPort.isResourceOwnedByScope(
                    command.actorScopeKey(),
                    command.actorScopeId(),
                    command.scopeKey(),
                    command.scopeId()
            );
            if (!owned) {
                throw new IdentityServiceException(
                        new IdentityServiceError.ScopeOwnershipViolation(
                                command.actorScopeId(),
                                command.scopeId()
                        ),
                        "Target resource does not belong to actor scope"
                );
            }
        }
        expirePreviousAssignmentIfDue(command, scope);
        if (assignments.existsCurrent(command.userId(), command.roleCode(), scope)) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AccessAssignmentExists(
                            command.userId().getValue(), command.roleCode(), scope.scopeId()
                    ),
                    "Access assignment already exists"
            );
        }
        AccessAssignment saved = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                command.roleCode(),
                scope,
                command.assignedBy(),
                command.expiresAt()
        ));
        String roleCode = command.roleCode();
        if (RoleAdministrationPolicy.requiresSessionRevocation(roleCode)) {
            user.invalidateAuthenticationSessions();
            users.save(user);
            if (sessions != null) {
                var userId = user.getId();
                String userIdValue = userId.getValue();
                sessions.revokeAll(userIdValue);
            }
        }
        return AccessAssignmentResult.from(saved);
    }

    private void expirePreviousAssignmentIfDue(GrantAccessCommand command, AccessScope scope) {
        Instant now = Instant.now();
        assignments.findCurrent(command.userId(), command.roleCode(), scope)
                .filter(assignment -> assignment.expireIfDue(now))
                .ifPresent(assignments::save);
    }
}
