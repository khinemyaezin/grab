package com.identity.domain.service;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.repository.RoleDelegationRuleRepository;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class RuleBasedRoleDelegationPolicy implements RoleDelegationPolicy {
    private final RoleDelegationRuleRepository rules;

    public RuleBasedRoleDelegationPolicy(RoleDelegationRuleRepository rules) {
        this.rules = Objects.requireNonNull(rules, "role delegation rules are required");
    }

    @Override
    public void requireCanDelegate(Set<String> actorRoleCodes, String requestedRoleCode) {
        Set<String> normalizedActorRoles = (actorRoleCodes == null ? Set.<String>of() : actorRoleCodes).stream()
                .map(RuleBasedRoleDelegationPolicy::normalize)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        String requested = normalize(requestedRoleCode);

        if (requested.isEmpty() || !rules.existsActiveRule(normalizedActorRoles, requested)) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccessRoleDelegationForbidden(requested),
                    "The requested role cannot be delegated by the actor"
            );
        }
    }

    private static String normalize(String roleCode) {
        return roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
    }
}
