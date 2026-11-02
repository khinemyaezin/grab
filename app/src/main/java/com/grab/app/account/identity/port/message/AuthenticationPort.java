package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.Optional;

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
            String username,
            String password
    ) {}

    record RefreshTokenMessage(
            String refreshToken
    ) {}

    record LogoutMessage(
            Id userId,
            String token
    ) {}

    record RegisterUserMessage(
            String username,
            String email,
            String rawPassword,
            String fullName
    ) {}

    record AuthTokensResult(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds,
            Id userId
    ) {}

    record UserRegistrationResult(
            Id userId,
            String username,
            String email,
            String status,
            Instant createdAt
    ) {}
}

