package com.identity.infrastructure.adapter;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.IdentityLookupPort;
import com.identity.infrastructure.entity.*;
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
        AccessAssignmentEntity anchor = accessAssignments
                .findForContext(context.assignmentId(), user.getUuid(), context.platformCode())
                .orElseThrow(this::invalidAccessContext);

        boolean matchesScope = anchor.getScopeKey().equals(context.scopeKey())
                && anchor.getScopeId().equals(context.scopeId());
        if (!matchesScope) {
            throw invalidAccessContext();
        }

        Set<RoleEntity> roles = accessAssignments.findEffectiveByUserAndPlatform(
                        user.getUuid(), context.platformCode(), Instant.now()
                ).stream()
                .filter(assignment -> assignment.getScopeKey().equals(context.scopeKey()))
                .filter(assignment -> assignment.getScopeId().equals(context.scopeId()))
                .map(AccessAssignmentEntity::getPlatformRole)
                .map(PlatformRoleEntity::getRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (roles.isEmpty()) {
            throw invalidAccessContext();
        }
        return roles;
    }

    private IdentityDomainValidationException invalidAccessContext() {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessCode("accessContext", "invalid"),
                "Access context is not active or valid"
        );
    }
}
