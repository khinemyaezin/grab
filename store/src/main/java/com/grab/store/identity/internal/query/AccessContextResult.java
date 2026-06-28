package com.grab.store.identity.internal.query;

public record AccessContextResult(
        String assignmentId,
        String platformCode,
        String roleCode,
        String scopeType,
        String scopeId,
        String expiresAt
) {
}
