package com.identity.application.model.write;

public record UserProfileResult(
        String id,
        String email,
        String status,
        String createdAt
) {
}
