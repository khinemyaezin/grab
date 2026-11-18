package com.identity.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.security.AccessContext;
import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.model.read.CredentialView;
import com.identity.application.model.read.ScopeCatalogView;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.ScopeHierarchy;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.adapter.persistence.entity.*;
import com.identity.adapter.persistence.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.adapter.persistence.repository.jpa.ExternalIdentityJpaRepository;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import com.identity.adapter.persistence.repository.jpa.UserJpaRepository;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityLookupQueryAdapter implements IdentityLookupQueryPort {

    private final UserJpaRepository users;
    private final ExternalIdentityJpaRepository externalIdentities;
    private final AccessAssignmentJpaRepository accessAssignments;
    private final ScopeCatalogQueryPort scopes;

    public IdentityLookupQueryAdapter(UserJpaRepository users, ExternalIdentityJpaRepository externalIdentities,
            AccessAssignmentJpaRepository accessAssignments,
            ScopeCatalogQueryPort scopes) {
        this.users = users;
        this.externalIdentities = externalIdentities;
        this.accessAssignments = accessAssignments;
        this.scopes = scopes;
    }

    @Override
    public Optional<CredentialView> findCredentials(String normalizedEmail) {
        return users.findCredentialView(normalizedEmail);
    }

    @Override
    public Optional<ActiveSubjectView> findActiveSubject(Id userId) {
        String uuid = userId.getValue();
        return users.findSubject(uuid, UserStatus.ACTIVE);
    }

    @Override
    public Optional<AuthenticatedActor> resolveSeller(Id userId, Id assignmentId) {
        String userKey = userId.getValue();
        Optional<UserEntity> found = users.findByUuid(userKey);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        UserEntity user = found.orElseThrow();
        if (user.getStatus() != UserStatus.ACTIVE) {
            return Optional.empty();
        }
        AccessContext context = null;
        if (assignmentId != null) {
            String assignmentKey = assignmentId.getValue();
            Instant now = Instant.now();
            Optional<AccessAssignmentEntity> anchor = accessAssignments.findEffectiveByUser(userKey, now).stream()
                    .filter(assignment -> assignment.getUuid().equals(assignmentKey))
                    .findFirst();
            if (anchor.isEmpty()) {
                return Optional.empty();
            }
            AccessAssignmentEntity assignment = anchor.orElseThrow();
            context = new AccessContext(assignmentKey, assignment.getScopeKey(), assignment.getScopeId());
        }
        try {
            AuthenticatedActor actor = resolveForUser(user, "seller-oidc", userKey, context);
            return Optional.of(actor);
        } catch (IdentityDomainValidationException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext) {
        return users.findByUuid(userId)
                .map(user -> resolveForUser(user, issuer, userId, accessContext));
    }

    @Override
    public Optional<AuthenticatedActor> resolveByExternalIdentity(String issuer, String subject, Set<String> entitlements, AccessContext accessContext) {
        return externalIdentities.findByIssuerAndSubject(issuer, subject)
                .filter(ExternalIdentityEntity::isEnabled)
                .map(ExternalIdentityEntity::getUser)
                .map(user -> resolveForUser(user, issuer, subject, accessContext));
    }

    private AuthenticatedActor resolveForUser(UserEntity user, String issuer, String subject, AccessContext accessContext) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccountNotActive(user.getUuid()), "Account is not active");
        }

        AccessContext effectiveContext = accessContext == null ? null : effectiveContext(user, accessContext);
        Set<RoleEntity> effectiveRoles = effectiveContext == null
                ? new LinkedHashSet<>()
                : scopedRoles(user, effectiveContext);

        Set<String> roleCodes = effectiveRoles.stream()
                .filter(RoleEntity::isActive)
                .map(RoleEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> authorities = effectiveRoles.stream()
                .filter(RoleEntity::isActive)
                .flatMap(r -> r.getAuthorities().stream())
                .filter(AuthorityEntity::isEffective)
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());

        return new AuthenticatedActor(
                user.getUuid(),
                issuer,
                subject,
                user.getEmail(),
                roleCodes,
                authorities,
                effectiveContext,
                user.getAuthenticationVersion()
        );
    }

    private AccessContext effectiveContext(UserEntity user, AccessContext requestedContext) {
        if (requestedContext.assignmentId() == null) {
            throw invalidAccessContext();
        }
        if (requestedContext.scopeKey() != null) {
            ScopeCatalogView catalog = scopes.load();
            ScopeHierarchy hierarchy = catalog.hierarchy();
            if (!hierarchy.isEffective(requestedContext.scopeKey())) {
                throw invalidAccessContext();
            }
        }
        AccessAssignmentEntity anchor = accessAssignments.findEffectiveByUser(user.getUuid(), Instant.now()).stream()
                .filter(assignment -> assignment.getUuid().equals(requestedContext.assignmentId()))
                .findFirst()
                .orElseThrow(this::invalidAccessContext);
        boolean requestedScopeMatches = requestedContext.scopeKey() == null && requestedContext.scopeId() == null
                || anchor.getScopeKey().equals(requestedContext.scopeKey())
                && anchor.getScopeId().equals(requestedContext.scopeId());
        if (!requestedScopeMatches) {
            throw invalidAccessContext();
        }
        return new AccessContext(requestedContext.assignmentId(), anchor.getScopeKey(), anchor.getScopeId());
    }

    private Set<RoleEntity> scopedRoles(UserEntity user, AccessContext context) {
        ScopeCatalogView catalog = scopes.load();
        ScopeHierarchy hierarchy = catalog.hierarchy();
        if (!hierarchy.isEffective(context.scopeKey())) {
            throw invalidAccessContext();
        }
        AccessAssignmentEntity anchor = accessAssignments
                .findByUuidAndUser_Uuid(context.assignmentId(), user.getUuid())
                .orElseThrow(this::invalidAccessContext);

        boolean matchesScope = anchor.getScopeKey().equals(context.scopeKey())
                && anchor.getScopeId().equals(context.scopeId());
        if (!matchesScope) {
            throw invalidAccessContext();
        }

        Set<RoleEntity> roles = accessAssignments.findEffectiveByUser(
                        user.getUuid(), Instant.now()
                ).stream()
                .filter(assignment -> assignment.getScopeKey().equals(context.scopeKey()))
                .filter(assignment -> assignment.getScopeId().equals(context.scopeId()))
                .map(AccessAssignmentEntity::getRole)
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
