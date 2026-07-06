package com.grab.store.identity.internal.api.rest.dto.response;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInMs,
        String userId,
        String email,
        Set<String> roles,
        String status,
        boolean contextSelectionRequired
) {
}
