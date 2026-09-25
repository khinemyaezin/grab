package com.identity.domain.model;

import com.grab.framework.security.AuthorityDefinition;

import java.util.Objects;

public record Authority(String code, String name, String description) {
    public Authority {
        Objects.requireNonNull(code, "authority code is required");
        Objects.requireNonNull(name, "authority name is required");
        if (code.isBlank()) {
            throw new IllegalArgumentException("authority code must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("authority name must not be blank");
        }
    }

    public static Authority from(AuthorityDefinition definition) {
        Objects.requireNonNull(definition, "authority definition is required");
        return new Authority(definition.code(), definition.name(), definition.description());
    }
}
