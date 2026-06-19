package com.grab.store.identity.internal.query;

import java.util.Set;

public record ListRolesResult(
        String code,
        String name,
        String description,
        boolean active,
        Set<String> authorities
) {
}
