package com.grab.app.platform.seller.port.outbound;

import java.util.Set;

public interface SellerAuthOutboundPort {

    SellerAuthTokensResult login(String email, String password);

    SellerAuthTokensResult refreshToken(String refreshToken);

    void logout(String refreshToken);

    SellerUserRegistrationResult registerUser(String email, String password);

    record SellerAuthTokensResult(
            String accessToken,
            String refreshToken,
            long expiresInMs,
            String userId,
            String email,
            Set<String> roles,
            String status,
            boolean contextSelectionRequired
    ) {}

    record SellerUserRegistrationResult(
            String id,
            String email,
            String status,
            String createdAt
    ) {}
}

