package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.PlatformRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ReplaceAccessService implements ReplaceAccessUseCase {
    private final UserRepository users;
    private final PlatformRepository platforms;
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final IdGenerator ids;

    @Override
    public AccessAssignmentResult execute(ReplaceAccessCommand command) {
        users.findById(command.userId()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.userId().getValue()),
                "User not found"
        ));

        Platform platform = platforms.findByCode(command.platformCode())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Platform not found"
                ));

        AccessScope scope = AccessScope.from(command.scopeKey(), command.scopeId());
        Instant now = Instant.now();

        List<AccessAssignment> currentAssignments = assignments.findCurrentByUserPlatformAndScope(
                command.userId(),
                platform.getCode(),
                scope
        );

        String replacementRole = command.replacementRoleCode();
        if (replacementRole != null && !replacementRole.isBlank()) {
            replacementRole = platform.requireSupportedRole(replacementRole);
        }

        String previousRole = command.previousRoleCode();
        if (previousRole != null && !previousRole.isBlank()) {
            previousRole = platform.requireSupportedRole(previousRole);
        }

        AccessAssignment existingReplacement = null;
        if (replacementRole != null && !replacementRole.isBlank()) {
            for (AccessAssignment current : currentAssignments) {
                if (current.getRoleCode().equalsIgnoreCase(replacementRole) && current.isEffectiveAt(now)) {
                    existingReplacement = current;
                    break;
                }
            }
        }

        AccessAssignment lastRevoked = null;
        for (AccessAssignment current : currentAssignments) {
            if (existingReplacement != null && current.getId().equals(existingReplacement.getId())) {
                continue;
            }

            boolean shouldRevoke = (previousRole != null && !previousRole.isBlank())
                    ? current.getRoleCode().equalsIgnoreCase(previousRole)
                    : true;

            if (shouldRevoke) {
                lastRevoked = retireAssignment(current, now);
            }
        }

        if (existingReplacement != null) {
            return AccessAssignmentResult.from(existingReplacement, now);
        }

        if (replacementRole == null || replacementRole.isBlank()) {
            if (lastRevoked != null) {
                return AccessAssignmentResult.from(lastRevoked, now);
            }
            return AccessAssignmentResult.revoked(
                    command.userId().getValue(),
                    platform.getCode(),
                    scope.key().value(),
                    scope.scopeId()
            );
        }

        AccessAssignment replacement = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                platform,
                replacementRole,
                scope,
                null,
                null
        ));

        return AccessAssignmentResult.from(replacement, now);
    }

    private AccessAssignment retireAssignment(AccessAssignment assignment, Instant now) {
        if (!assignment.expireIfDue(now)) {
            assignment.revoke();
        }
        AccessAssignment retired = assignments.save(assignment);
        sessions.revokeByAssignment(retired.getId().getValue());
        return retired;
    }
}
