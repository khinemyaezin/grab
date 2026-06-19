package com.grab.store.identity.internal.api.rest.dto.response;

import java.util.Set;

public record UserMutationResponse(
        String id,
        String email,
        Set<String> roles,
        String status,
        String createdAt
) {
}
