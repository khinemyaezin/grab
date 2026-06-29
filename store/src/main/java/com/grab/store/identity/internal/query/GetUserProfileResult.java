package com.grab.store.identity.internal.query;

public record GetUserProfileResult(
        String id,
        String email,
        String status,
        String createdAt
) {
}
