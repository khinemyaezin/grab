package com.identity.domain.policy.impl;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.Role;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.model.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
            Set<String> requestedAuthorityCodes
    ) {
        Set<Authority> authorities = requireActiveAuthorities(requestedAuthorityCodes);
        return Role.createCustom(roleId, code, name, description, authorities);
    }

    public void changeAuthority(Role role, String authorityCode, boolean assign) {
        Set<Authority> resolvedAuthorities = requireActiveAuthorities(Set.of(authorityCode));
        Authority authority = resolvedAuthorities.iterator().next();
        if (assign) {
            role.assignAuthority(authority);
        } else {
            role.revokeAuthority(authority);
        }
    }

    private Set<Authority> requireActiveAuthorities(Set<String> requestedAuthorityCodes) {
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
        Set<Authority> activeAuthorities = authorities.findActiveByCodes(normalizedCodes);
        Set<String> activeCodes = activeAuthorities.stream()
                .map(Authority::getCode)
                .collect(Collectors.toSet());
        if (!activeCodes.equals(normalizedCodes)) {
            LinkedHashSet<String> unavailableCodes = new LinkedHashSet<>(normalizedCodes);
            unavailableCodes.removeAll(activeCodes);
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AuthoritiesUnavailable(Set.copyOf(unavailableCodes)),
                    "One or more authorities are unavailable"
            );
        }
        return Set.copyOf(activeAuthorities);
    }
}
