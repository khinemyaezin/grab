package com.identity.application.model.read;

public record ListUsersResult(
        String id,
        String email,
        String status,
        String createdAt
) {
}
