package com.grab.store.identity.internal.api.rest.dto.response;

public record UserProfileResponse(
        String id,
        String email,
        String status,
        String createdAt
) {
}
