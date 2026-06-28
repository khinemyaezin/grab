package com.grab.store.identity.internal.query;

public record AccessContextResult(
        String assignmentId,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        String expiresAt
) {
}
