package com.identity.domain.aggregate;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.grab.framework.security.AuthorityDefinition;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class Authority extends Entity<Id> {
    private final String code;
    private final String category;
    private final String name;
    private final String description;
    private final boolean active;

    private Authority(Id id, String code, String category, String name, String description, boolean active) {
        super(Objects.requireNonNull(id, "authority id is required"));
        this.code = validateCode(code);
        this.category = validateCategory(category);
        this.name = validateName(name);
        this.description = description;
        this.active = active;
    }

    public static Authority from(Id id, String moduleKey, AuthorityDefinition definition) {
        Objects.requireNonNull(definition, "authority definition is required");
        return create(id, definition.code(), moduleKey, definition.name(), definition.description());
    }

    public static Authority create(
            Id id,
            String code,
            String category,
            String name,
            String description
    ) {
        return new Authority(id, code, category, name, description, true);
    }

    public static Authority rehydrate(
            Id id,
            String code,
            String category,
            String name,
            String description,
            boolean active
    ) {
        return new Authority(id, code, category, name, description, active);
    }

    private static String validateCode(String value) {
        Objects.requireNonNull(value, "authority code is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException("authority code must not be blank");
        }
        return value;
    }

    private static String validateCategory(String value) {
        Objects.requireNonNull(value, "authority category is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException("authority category must not be blank");
        }
        return value;
    }

    private static String validateName(String value) {
        Objects.requireNonNull(value, "authority name is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException("authority name must not be blank");
        }
        return value;
    }
}
