package com.grab.store.identity.internal.command;

import java.util.Set;

public record RoleResult(
        String code,
        String name,
        String description,
        String kind,
        boolean active,
        boolean assignable,
        Set<String> authorities,
        Set<String> platformCodes
) {
}
