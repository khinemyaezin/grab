package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.ReplaceAccessCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.policy.AccessPlacementPolicy;
import com.identity.domain.policy.AccessPlacementPolicyResolver;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ReplaceAccessCommandHandler
        implements CommandHandler<ReplaceAccessCommand, AccessAssignmentResult> {
    private final UserRepository users;
    private final PlatformRepository platforms;
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final IdGenerator ids;
    private final AccessPlacementPolicyResolver placementPolicies;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(ReplaceAccessCommand command) {
        users.findById(command.userId()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.userId().getValue()),
                "User not found"
        ));

        Platform platform = platforms.findByCode(command.platformCode())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Platform not found"
                ));

        AccessPlacementPolicy placementPolicy = placementPolicies.resolve(
                command.replacementRoleCode()
        );

        if (placementPolicy == null) {
            throw new IdentityServiceException(
                    new IdentityServiceError.RoleNotFound(command.replacementRoleCode()),
                    "Access placement policy not found"
            );
        }

        AccessPlacementPolicy.AccessPlacementPlan plan = placementPolicy.plan(
                platform,
                AccessScope.from(command.scopeKey(), command.scopeId())
        );

        revokePreviousAccess(command, platform, plan);

        AccessAssignment replacement = resolveReplacement(command, platform, plan);
        return AccessAssignmentResult.from(replacement);
    }

    private AccessAssignment resolveReplacement(
            ReplaceAccessCommand command,
            Platform platform,
            AccessPlacementPolicy.AccessPlacementPlan plan
    ) {
        AccessAssignment current = assignments.findCurrent(
                command.userId(),
                platform.getCode(),
                plan.replacementRoleCode(),
                plan.replacementScope()
        ).orElse(null);

        Instant now = Instant.now();
        if (current != null && current.isEffectiveAt(now)) {
            return current;
        }
        if (current != null) {
            retireInactiveReplacement(current, now);
        }

        return assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                platform,
                plan.replacementRoleCode(),
                plan.replacementScope(),
                null,
                null
        ));
    }

    private void retireInactiveReplacement(AccessAssignment assignment, Instant now) {
        if (!assignment.expireIfDue(now)) {
            assignment.revoke();
        }
        AccessAssignment retired = assignments.save(assignment);
        sessions.revokeByAssignment(retired.getId().getValue());
    }

    private void revokePreviousAccess(
            ReplaceAccessCommand command,
            Platform platform,
            AccessPlacementPolicy.AccessPlacementPlan plan
    ) {
        assignments.findCurrent(
                command.userId(),
                platform.getCode(),
                plan.previousRoleCode(),
                plan.previousScope()
        ).ifPresent(assignment -> {
            assignment.revoke();
            AccessAssignment revoked = assignments.save(assignment);
            sessions.revokeByAssignment(revoked.getId().getValue());
        });
    }

    @Override
    public Class<ReplaceAccessCommand> getCommandType() {
        return ReplaceAccessCommand.class;
    }
}
