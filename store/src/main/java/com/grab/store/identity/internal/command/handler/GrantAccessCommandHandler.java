package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.GrantAccessCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class GrantAccessCommandHandler implements CommandHandler<GrantAccessCommand, AccessAssignmentResult> {
    private final UserRepository users;
    private final PlatformRepository platforms;
    private final RoleRepository roles;
    private final AccessAssignmentRepository assignments;
    private final RoleDelegationPolicy delegationPolicy;
    private final IdGenerator ids;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(GrantAccessCommand command) {
        users.findById(command.userId()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.userId().getValue()),
                "User not found"
        ));
        Platform platform = platforms.findByCode(command.platformCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Platform not found"
                )
        );
        Role role = roles.findByCode(command.roleCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(command.roleCode()),
                        "Role not found"
                )
        );
        role.requireAssignable();
        AccessScope scope = AccessScope.from(command.scopeKey(), command.scopeId());
        AccessScope.from(command.actorScopeKey(), command.actorScopeId()).requireEncompasses(scope);
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), command.roleCode());
        expirePreviousAssignmentIfDue(command, scope);
        if (assignments.existsCurrent(command.userId(), command.platformCode(), command.roleCode(), scope)) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AccessAssignmentExists(
                            command.userId().getValue(), command.platformCode(), command.roleCode(), scope.scopeId()
                    ),
                    "Access assignment already exists"
            );
        }
        AccessAssignment saved = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                platform,
                command.roleCode(),
                scope,
                command.assignedBy(),
                command.expiresAt()
        ));
        return AccessAssignmentResult.from(saved);
    }

    private void expirePreviousAssignmentIfDue(GrantAccessCommand command, AccessScope scope) {
        Instant now = Instant.now();
        assignments.findCurrent(command.userId(), command.platformCode(), command.roleCode(), scope)
                .filter(assignment -> assignment.expireIfDue(now))
                .ifPresent(assignments::save);
    }

    @Override
    public Class<GrantAccessCommand> getCommandType() {
        return GrantAccessCommand.class;
    }

}
