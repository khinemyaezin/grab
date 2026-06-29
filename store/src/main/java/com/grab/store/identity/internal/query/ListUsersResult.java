package com.grab.store.identity.internal.query;

public record ListUsersResult(
        String id,
        String email,
        String status,
        String createdAt
) {
}
