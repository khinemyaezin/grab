package com.grab.store.identity.internal.command;

import java.util.Set;

public record UserProfileResult(
        String id,
        String email,
        Set<String> roles,
        String status,
        String createdAt
) {
}
