package com.identity.application.model.read;

import java.util.Set;

public record RoleListView(
        String code,
        String name,
        String description,
        String kind,
        boolean active,
        boolean assignable,
        Set<String> authorityCodes,
        Set<String> platformCodes
) {
    public RoleListView(
            String code,
            String name,
            String description,
            String kind,
            boolean active,
            boolean assignable,
            Set<String> authorityCodes
    ) {
        this(code, name, description, kind, active, assignable, authorityCodes, Set.of());
    }
}
