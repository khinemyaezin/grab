package com.identity.domain.valueobject;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.Locale;

public record ScopeKey(String value) {
    public static final String GLOBAL_VALUE = "global";

    public ScopeKey {
        if (value == null) {
            throw invalid(null);
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!GLOBAL_VALUE.equals(value)
                && !value.matches("[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)+")) {
            throw invalid(value);
        }
    }

    public static ScopeKey global() {
        return new ScopeKey(GLOBAL_VALUE);
    }

    public boolean isGlobal() {
        return GLOBAL_VALUE.equals(value);
    }

    private static IdentityDomainValidationException invalid(String value) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidScopeKey(String.valueOf(value)),
                "Invalid access scope key"
        );
    }
}
