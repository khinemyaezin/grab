package com.grab.store.identity.internal.api.rest.dto.response;

import java.util.Set;

public record RoleResponse(
        String code,
        String name,
        String description,
        boolean active,
        Set<String> authorities
) {
}
