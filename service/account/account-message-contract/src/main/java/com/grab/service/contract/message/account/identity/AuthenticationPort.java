package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Set;

/**
 * Universal Java Port Interface for Identity Authentication and Token operations.
 * Replaces legacy AuthController.
 */
public interface AuthenticationPort extends Remote {

    @Deprecated
    AuthTokensResult login(LoginMessage message) throws RemoteException;

    @Deprecated
    AuthTokensResult refreshToken(RefreshTokenMessage message) throws RemoteException;

    @Deprecated
    void logout(LogoutMessage message) throws RemoteException;

    UserRegistrationResult registerUser(RegisterUserMessage message) throws RemoteException;

    record LoginMessage(
            String email,
            String password
    ) implements Serializable {}

    record RefreshTokenMessage(
            String refreshToken
    ) implements Serializable {}

    record LogoutMessage(
            String refreshToken
    ) implements Serializable {}

    record RegisterUserMessage(
            String email,
            String password
    ) implements Serializable {
        @Override
        public String toString() {
            return "RegisterUserMessage[redacted]";
        }
    }

    record AuthTokensResult(
            String accessToken,
            String refreshToken,
            long expiresInMs,
            String userId,
            String email,
            Set<String> roles,
            String status,
            boolean contextSelectionRequired
    ) implements Serializable {}

    record UserRegistrationResult(
            String id,
            String email,
            String status,
            String createdAt
    ) implements Serializable {}
}
