package com.grab.store.shared.security;

import com.grab.framework.security.*;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.infrastructure.entity.*;
import com.identity.infrastructure.repository.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdentityResolverAdapter implements PlatformIdentityResolver {
    private final UserJpaRepository users;
    private final ExternalIdentityJpaRepository externalIdentities;
    private final ExternalEntitlementMappingJpaRepository entitlementMappings;
    private final AccessAssignmentJpaRepository accessAssignments;
    private final LocalJwtProperties properties;

    @Override
    @Transactional(transactionManager = "identityTransactionManager", readOnly = true)
    public AuthenticatedActor resolve(ExternalPrincipal principal) {
        UserEntity user = properties.issuer().equals(principal.issuer())
                ? users.findByUuid(principal.subject())
                .orElseThrow(this::notLinked)
                : externalIdentities.findByIssuerAndSubject(principal.issuer(), principal.subject())
                .map(ExternalIdentityEntity::getUser)
                .orElseThrow(this::notLinked);

        if (user.getStatus() != com.identity.domain.enums.UserStatus.ACTIVE) {
            throw new IdentityAuthenticationException(new IdentitySecurityError.AccountNotActive(), "Account is not active");
        }

        boolean selectionOnly = principal.accessContext().isEmpty()
                && accessAssignments.existsByUser_Uuid(user.getUuid());
        Set<RoleEntity> effectiveRoles = principal.accessContext()
                .map(context -> scopedRoles(user, context))
                .orElseGet(() -> selectionOnly
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(user.getRoles()));
        if (!selectionOnly && principal.accessContext().isEmpty() && !principal.entitlements().isEmpty()) {
            entitlementMappings.findByIssuerAndEntitlementIn(principal.issuer(), principal.entitlements())
                    .stream()
                    .map(ExternalEntitlementMappingEntity::getRole)
                    .forEach(effectiveRoles::add);
        }
        Set<String> roleCodes = effectiveRoles.stream()
                .filter(RoleEntity::isActive)
                .map(RoleEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> authorities = effectiveRoles.stream()
                .filter(RoleEntity::isActive)
                .flatMap(r -> r.getAuthorities().stream())
                .filter(AuthorityEntity::isActive)
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());

        return new AuthenticatedActor(
                user.getUuid(),
                principal.issuer(),
                principal.subject(),
                user.getEmail(),
                roleCodes,
                authorities,
                principal.accessContext()
        );
    }

    private Set<RoleEntity> scopedRoles(UserEntity user, AccessContext context) {
        AccessAssignmentEntity assignment = accessAssignments
                .findForContext(context.assignmentId(), user.getUuid(), context.platformCode())
                .orElseThrow(this::invalidAccessContext);
        boolean matchesScope = assignment.getScopeKey().equals(context.scopeKey())
                && assignment.getScopeId().equals(context.scopeId());
        boolean effective = assignment.getStatus() == com.identity.domain.enums.AccessAssignmentStatus.ACTIVE
                && (assignment.getExpiresAt() == null || assignment.getExpiresAt().isAfter(Instant.now()))
                && assignment.getPlatformRole().isActive()
                && assignment.getPlatformRole().getPlatform().isActive()
                && assignment.getPlatformRole().getRole().isActive();
        if (!matchesScope || !effective) {
            throw invalidAccessContext();
        }
        return new LinkedHashSet<>(Set.of(assignment.getPlatformRole().getRole()));
    }

    private IdentityAuthenticationException notLinked() {
        return new IdentityAuthenticationException(new IdentitySecurityError.IdentityNotLinked(), "Identity is not linked");
    }

    private IdentityAuthenticationException invalidAccessContext() {
        return new IdentityAuthenticationException(
                new IdentitySecurityError.InvalidAccessContext(),
                "Access context is not active"
        );
    }
}
