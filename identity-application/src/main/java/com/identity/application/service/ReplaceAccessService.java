package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.policy.AuthoritySelectionPolicy;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.valueobject.AccessScope;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class ReplaceAccessService implements ReplaceAccessUseCase {
    private final SecurityCatalogRepository catalogs;
    private final UserRepository users;
    private final RoleRepository roles;
    private final AuthorityRepository authorities;
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final IdGenerator ids;

    public ReplaceAccessService(
            UserRepository users,
            RoleRepository roles,
            AuthorityRepository authorities,
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            IdGenerator ids,
            SecurityCatalogRepository catalogs
    ) {
        this.catalogs = catalogs;
        this.users = users;
        this.roles = roles;
        this.authorities = authorities;
        this.assignments = assignments;
        this.sessions = sessions;
        this.ids = ids;
    }

    @Override
    public AccessAssignmentResult execute(ReplaceAccessCommand command) {
        var userLookup = users.findByIdForUpdate(command.userId());
        if (userLookup.isEmpty()) {
            userLookup = users.findById(command.userId());
        }
        var user = userLookup.orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.userId().getValue()),
                "User not found"
        ));

        AccessScope scope = AccessScope.from(command.scopeKey(), command.scopeId());
        Instant now = Instant.now();

        List<AccessAssignment> currentAssignments = assignments.findCurrentByUserAndScope(
                command.userId(),
                scope
        );

        String replacementRole = command.replacementRoleCode();
        if (replacementRole != null && !replacementRole.isBlank()) {
            SecurityCatalog catalog = catalogs.loadForUpdate();
            catalog.requireEffectiveScope(scope.key().value());
        }
        if (replacementRole != null && !replacementRole.isBlank()) {
            replacementRole = replacementRole.trim().toUpperCase(Locale.ROOT);
        }

        String previousRole = command.previousRoleCode();
        if (previousRole != null && !previousRole.isBlank()) {
            previousRole = previousRole.trim().toUpperCase(Locale.ROOT);
        }

        AccessAssignment existingReplacement = null;
        if (replacementRole != null && !replacementRole.isBlank()) {
            ensureRoleExists(replacementRole, command.authorityCodes());
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

            boolean shouldRevoke = previousRole == null || previousRole.isBlank() || current.getRoleCode().equalsIgnoreCase(previousRole);

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
                    scope.key().value(),
                    scope.scopeId()
            );
        }

        AccessAssignment replacement = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                replacementRole,
                scope,
                null,
                null
        ));

        if (RoleAdministrationPolicy.requiresSessionRevocation(replacementRole)) {
            user.invalidateAuthenticationSessions();
            users.save(user);
            var userId = user.getId();
            String userIdValue = userId.getValue();
            sessions.revokeAll(userIdValue);
        }

        return AccessAssignmentResult.from(replacement, now);
    }

    private void ensureRoleExists(String roleCode, Set<String> requestedAuthorityCodes) {
        Set<String> validCodes = AuthoritySelectionPolicy.normalize(requestedAuthorityCodes);
        Set<Authority> activeAuthorities = validCodes.isEmpty() ? Set.of() : authorities.findActiveByCodes(validCodes);
        AuthoritySelectionPolicy.requireComplete(validCodes, activeAuthorities);
        Optional<Role> existingRole = roles.findByCode(roleCode);
        if (existingRole.isPresent()) {
            existingRole.get().requireAssignable();
            return;
        }
        if (RoleAdministrationPolicy.RESERVED_SYSTEM_ROLES.contains(roleCode)) {
            throw new IdentityServiceException(
                    new IdentityServiceError.RoleNotFound(roleCode),
                    "System role declaration is not ready"
            );
        }
        if (validCodes.isEmpty()) {
            IdentityServiceError error = new IdentityServiceError.RoleNotFound(roleCode);
            throw new IdentityServiceException(error, "Role cannot be created without active authorities");
        }

        Role newRole = Role.createCustom(
                ids.generateId(),
                roleCode,
                roleCode,
                null,
                activeAuthorities
        );
        roles.save(newRole);
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
