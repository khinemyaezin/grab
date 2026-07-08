package com.grab.store.identity.internal.command;

import java.util.Set;

public record AuthResult(
        String accessToken,
        String refreshToken,
        long expiresInMs,
        String userId,
        String email,
        Set<String> roles,
        String status,
        boolean contextSelectionRequired
) {}
