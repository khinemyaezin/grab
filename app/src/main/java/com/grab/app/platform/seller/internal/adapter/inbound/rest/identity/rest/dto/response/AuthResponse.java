package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response;

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
