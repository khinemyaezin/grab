package com.grab.app.identityaccount.identity.internal.api.rest.dto.response;

import java.util.Set;

public record RoleResponse(
        String code,
        String name,
        String description,
        String kind,
        boolean active,
        boolean assignable,
        Set<String> authorities
) {
}
