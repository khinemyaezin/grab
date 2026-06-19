package com.grab.store.identity.internal.api.rest.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UserProfileResponse(
        String id,
        String email,
        Set<String> roles,
        String status,
        LocalDateTime createdAt
) {
}

