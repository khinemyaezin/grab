package com.grab.store.identity.internal.command;

public record UserProfileResult(
        String id,
        String email,
        String status,
        String createdAt
) {
}
