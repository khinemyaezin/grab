package com.grab.framework.security;

import java.util.Locale;
import java.util.Objects;

/** External scope prerequisite and the minimum owner revision that satisfies it. */
public record SecurityDependency(String scopeKey, int minimumRevision) {
    public SecurityDependency {
        Objects.requireNonNull(scopeKey, "dependency scope key is required");
        scopeKey = scopeKey.trim().toLowerCase(Locale.ROOT);
        if (scopeKey.isBlank()) {
            throw new IllegalArgumentException("dependency scope key must not be blank");
        }
        if (minimumRevision < 1) {
            throw new IllegalArgumentException("dependency revision must be positive");
        }
    }

    public SecurityDependency(String scopeKey) {
        this(scopeKey, 1);
    }
}
