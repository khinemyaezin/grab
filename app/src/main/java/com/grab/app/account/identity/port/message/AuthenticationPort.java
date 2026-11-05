package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.util.Set;

/**
 * Universal Java Port Interface for Identity Authentication and Token operations.
 * Replaces legacy AuthController.
 */
public interface AuthenticationPort {

    AuthTokensResult login(LoginMessage message);

    AuthTokensResult refreshToken(RefreshTokenMessage message);

    void logout(LogoutMessage message);

    UserRegistrationResult registerUser(RegisterUserMessage message);

    record LoginMessage(
            String email,
            String password
    ) {}

    record RefreshTokenMessage(
            String refreshToken
    ) {}

    record LogoutMessage(
            String refreshToken
    ) {}

    record RegisterUserMessage(
            String email,
            String password
    ) {}

    record AuthTokensResult(
            String accessToken,
            String refreshToken,
            long expiresInMs,
            String userId,
            String email,
            Set<String> roles,
            String status,
            boolean contextSelectionRequired
    ) {}

    record UserRegistrationResult(
            String id,
            String email,
            String status,
            String createdAt
    ) {}
}
