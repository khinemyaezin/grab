package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response;

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
