package com.identity.domain.policy;

import com.identity.domain.aggregate.Authority;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AuthoritySelectionPolicy {
    private AuthoritySelectionPolicy() { }

    public static Set<String> normalize(Set<String> requested) {
        if (requested == null) return Set.of();
        Set<String> normalized = new HashSet<>();
        for (String code : requested) {
            String value = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
            if (value == null || !value.matches("[A-Z][A-Z0-9_]*")) {
                throw invalid(code);
            }
            normalized.add(value);
        }
        return Set.copyOf(normalized);
    }

    public static void requireComplete(Set<String> requested, Set<Authority> resolved) {
        Set<String> active = new HashSet<>();
        for (Authority authority : resolved) {
            if (authority.isActive()) active.add(authority.getCode());
        }
        for (String code : requested) {
            if (!active.contains(code)) throw invalid(code);
        }
    }

    private static IdentityDomainValidationException invalid(String code) {
        var error = new IdentityDomainError.InvalidAuthorityCode(code);
        return new IdentityDomainValidationException(error, "Authority is unknown, invalid, retired, or disabled");
    }
}
