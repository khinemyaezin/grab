package com.grab.framework.security;

import java.util.Locale;
import java.util.Objects;

public record ScopeDeclaration(String scopeKey, String parentScopeKey, Lifecycle lifecycle) {
    public enum Lifecycle { ACTIVE, RETIRED }

    public ScopeDeclaration {
        Objects.requireNonNull(scopeKey, "scope key is required");
        scopeKey = scopeKey.trim().toLowerCase(Locale.ROOT);
        if (scopeKey.isBlank()) {
            throw new IllegalArgumentException("scope key must not be blank");
        }
        if (parentScopeKey != null) {
            parentScopeKey = parentScopeKey.trim().toLowerCase(Locale.ROOT);
            if (parentScopeKey.isBlank()) {
                parentScopeKey = null;
            }
        }
        lifecycle = lifecycle == null ? Lifecycle.ACTIVE : lifecycle;
    }

    public ScopeDeclaration(String scopeKey, String parentScopeKey) {
        this(scopeKey, parentScopeKey, Lifecycle.ACTIVE);
    }
}
