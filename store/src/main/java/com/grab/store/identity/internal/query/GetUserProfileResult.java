package com.grab.store.identity.internal.query;

import java.util.Set;

public record GetUserProfileResult(
        String id,
        String email,
        Set<String> roles,
        String status,
        String createdAt
) {
}
