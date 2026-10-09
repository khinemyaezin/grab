package com.grab.framework.security.role;

import java.util.Objects;

public record RolePermissionReference(
        String owner,
        String code
) {
    public RolePermissionReference {
        Objects.requireNonNull(owner, "owner is required");
        Objects.requireNonNull(code, "code is required");
        owner = owner.trim().toLowerCase(java.util.Locale.ROOT);
        code = code.trim().toUpperCase(java.util.Locale.ROOT);
        if (owner.isBlank()) {
            throw new IllegalArgumentException("owner cannot be blank");
        }
        if (code.isBlank()) {
            throw new IllegalArgumentException("code cannot be blank");
        }
    }
}
