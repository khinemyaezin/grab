package com.grab.framework.security.role;

import java.util.Objects;
import java.util.Locale;

public record RolePermissionReference(
        String owner,
        String code
) {
    public RolePermissionReference {
        Objects.requireNonNull(owner, "owner is required");
        Objects.requireNonNull(code, "code is required");
        owner = owner.trim().toLowerCase(Locale.ROOT);
        code = code.trim().toUpperCase(Locale.ROOT);
        if (owner.isBlank()) {
            throw new IllegalArgumentException("owner cannot be blank");
        }
        if (code.isBlank()) {
            throw new IllegalArgumentException("code cannot be blank");
        }
    }
}
