package com.identity.domain.service;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long expiresInMs,
        boolean contextSelected
        ) {
}
