package com.identity.domain.service;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessRoleDelegationPolicy {
    private static final Set<String> UNRESTRICTED_DELEGATORS = Set.of("SUPER_ADMIN", "USER_ADMIN");
    private static final Map<String, Set<String>> DELEGABLE_ROLES = Map.of(
            "MERCHANT_OWNER", Set.of(
                    "MERCHANT_ADMIN",
                    "STOREFRONT_MANAGER",
                    "CATALOG_MANAGER",
                    "INVENTORY_MANAGER",
                    "ORDER_MANAGER"
            ),
            "MERCHANT_ADMIN", Set.of(
                    "STOREFRONT_MANAGER",
                    "CATALOG_MANAGER",
                    "INVENTORY_MANAGER",
                    "ORDER_MANAGER"
            )
    );

    public void requireCanDelegate(Set<String> actorRoleCodes, String requestedRoleCode) {
        Set<String> normalizedActorRoles = (actorRoleCodes == null ? Set.<String>of() : actorRoleCodes).stream()
                .map(this::normalize)
                .collect(Collectors.toUnmodifiableSet());

        String requested = normalize(requestedRoleCode);

        boolean unrestricted = normalizedActorRoles.stream()
                .anyMatch(UNRESTRICTED_DELEGATORS::contains);

        boolean explicitlyDelegable = normalizedActorRoles.stream()
                .map(role -> DELEGABLE_ROLES.getOrDefault(role, Set.of()))
                .anyMatch(roles -> roles.contains(requested));
                
        if (!unrestricted && !explicitlyDelegable) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccessRoleDelegationForbidden(requested),
                    "The requested role cannot be delegated by the actor"
            );
        }
    }

    private String normalize(String roleCode) {
        return roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
    }
}
