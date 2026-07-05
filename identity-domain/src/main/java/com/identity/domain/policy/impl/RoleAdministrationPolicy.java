package com.identity.domain.policy.impl;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.repository.AuthorityRepository;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class RoleAdministrationPolicy {
    private final AuthorityRepository authorities;

    public RoleAdministrationPolicy(AuthorityRepository authorities) {
        this.authorities = Objects.requireNonNull(authorities, "authority repository is required");
    }

    public Role createCustomRole(
            Id roleId,
            String code,
            String name,
            String description,
            Platform platform,
            Set<String> requestedAuthorityCodes
    ) {
        Set<String> authorityCodes = requireActiveAuthorities(requestedAuthorityCodes);
        platform.requireSupportedAuthorities(authorityCodes);
        Role role = Role.createCustom(roleId, code, name, description, authorityCodes);
        platform.addRole(role.getCode());
        return role;
    }

    public void changeAuthority(Role role, Platform platform, String authorityCode, boolean assign) {
        Set<String> authorityCodes = requireActiveAuthorities(Set.of(authorityCode));
        platform.requireSupportedAuthorities(authorityCodes);
        String normalizedAuthorityCode = authorityCodes.iterator().next();
        if (assign) {
            role.assignAuthority(normalizedAuthorityCode);
        } else {
            role.revokeAuthority(normalizedAuthorityCode);
        }
    }

    private Set<String> requireActiveAuthorities(Set<String> requestedAuthorityCodes) {
        Objects.requireNonNull(requestedAuthorityCodes, "authority codes are required");
        LinkedHashSet<String> normalizedCodes = new LinkedHashSet<>();
        for (String requestedAuthorityCode : requestedAuthorityCodes) {
            if (requestedAuthorityCode != null) {
                normalizedCodes.add(requestedAuthorityCode.trim().toUpperCase(Locale.ROOT));
            }
        }
        if (normalizedCodes.isEmpty()) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.RoleAuthoritiesRequired(),
                    "A custom role requires at least one authority"
            );
        }
        Set<String> activeCodes = authorities.findActiveCodes(normalizedCodes);
        if (!activeCodes.equals(normalizedCodes)) {
            LinkedHashSet<String> unavailableCodes = new LinkedHashSet<>(normalizedCodes);
            unavailableCodes.removeAll(activeCodes);
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AuthoritiesUnavailable(Set.copyOf(unavailableCodes)),
                    "One or more authorities are unavailable"
            );
        }
        return Set.copyOf(normalizedCodes);
    }
}
