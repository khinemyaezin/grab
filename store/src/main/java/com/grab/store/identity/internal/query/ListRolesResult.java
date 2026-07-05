package com.grab.store.identity.internal.query;

import java.util.Set;

public record ListRolesResult(
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
