package com.grab.framework.security;

import java.util.Locale;
import java.util.Objects;

public record AuthorityDefinition(
        String code,
        String name,
        String description,
        String category,
        ScopeDeclaration.Lifecycle lifecycle
) {
    public AuthorityDefinition {
        Objects.requireNonNull(code, "authority code is required");
        Objects.requireNonNull(name, "authority name is required");
        category = category == null || category.isBlank() ? "default" : category.trim().toLowerCase(Locale.ROOT);
        lifecycle = lifecycle == null ? ScopeDeclaration.Lifecycle.ACTIVE : lifecycle;
        code = code.trim().toUpperCase(Locale.ROOT);
        if (code.isBlank()) {
            throw new IllegalArgumentException("authority code must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("authority name must not be blank");
        }
    }

    public AuthorityDefinition(String code, String name, String description) {
        this(code, name, description, "default", ScopeDeclaration.Lifecycle.ACTIVE);
    }
}
