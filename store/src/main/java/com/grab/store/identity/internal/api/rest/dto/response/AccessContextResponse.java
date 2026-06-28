package com.grab.store.identity.internal.api.rest.dto.response;

public record AccessContextResponse(
        String assignmentId,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        String expiresAt
) {
}
