package com.identity.infrastructure.adapter;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.IdentityLookupPort;
import com.identity.infrastructure.entity.AccessAssignmentEntity;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.ExternalEntitlementMappingEntity;
import com.identity.infrastructure.entity.ExternalIdentityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.infrastructure.repository.jpa.ExternalEntitlementMappingJpaRepository;
import com.identity.infrastructure.repository.jpa.ExternalIdentityJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class IdentityLookupAdapter implements IdentityLookupPort {

    private final UserJpaRepository users;
    private final ExternalIdentityJpaRepository externalIdentities;
    private final ExternalEntitlementMappingJpaRepository entitlementMappings;
    private final AccessAssignmentJpaRepository accessAssignments;

    @Override
    public Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext) {
        return users.findByUuid(userId)
                .map(user -> resolveForUser(user, issuer, userId, accessContext, Set.of()));
    }

    @Override
    public Optional<AuthenticatedActor> resolveByExternalIdentity(String issuer, String subject, Set<String> entitlements, AccessContext accessContext) {
        return externalIdentities.findByIssuerAndSubject(issuer, subject)
                .map(ExternalIdentityEntity::getUser)
                .map(user -> resolveForUser(user, issuer, subject, accessContext, entitlements));
    }

    private AuthenticatedActor resolveForUser(UserEntity user, String issuer, String subject, AccessContext accessContext, Set<String> entitlements) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccountNotActive(user.getUuid()), "Account is not active");
        }

        Set<RoleEntity> effectiveRoles = accessContext != null
                ? scopedRoles(user, accessContext)
                : new LinkedHashSet<>();

        if (accessContext == null && !entitlements.isEmpty()) {
            entitlementMappings.findByIssuerAndEntitlementIn(issuer, entitlements)
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
                issuer,
                subject,
                user.getEmail(),
                roleCodes,
                authorities,
                accessContext
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

    private IdentityDomainValidationException invalidAccessContext() {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessCode("accessContext", "invalid"),
                "Access context is not active or valid"
        );
    }
}
