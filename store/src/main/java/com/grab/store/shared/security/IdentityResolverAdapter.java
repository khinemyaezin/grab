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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdentityResolverAdapter implements PlatformIdentityResolver {
    private final UserJpaRepository users;
    private final ExternalIdentityJpaRepository externalIdentities;
    private final ExternalEntitlementMappingJpaRepository entitlementMappings;
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

        Set<RoleEntity> effectiveRoles = new LinkedHashSet<>(user.getRoles());
        if (!principal.entitlements().isEmpty()) {
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

        return new AuthenticatedActor(user.getUuid(), principal.issuer(), principal.subject(), user.getEmail(), roleCodes, authorities);
    }

    private IdentityAuthenticationException notLinked() {
        return new IdentityAuthenticationException(new IdentitySecurityError.IdentityNotLinked(), "Identity is not linked");
    }
}
