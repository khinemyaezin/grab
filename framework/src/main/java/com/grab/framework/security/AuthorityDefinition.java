package com.grab.framework.security;

import java.util.Objects;

public record AuthorityDefinition(String code, String name, String description) {
    public AuthorityDefinition {
        Objects.requireNonNull(code, "authority code is required");
        Objects.requireNonNull(name, "authority name is required");
        if (code.isBlank()) {
            throw new IllegalArgumentException("authority code must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("authority name must not be blank");
        }
    }
}
