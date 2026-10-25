package com.grab.app.identityaccount.identity.internal.api.rest.dto.response;

public record AccessAssignmentResponse(
        String id,
        String userId,
        String roleCode,
        String scopeKey,
        String scopeId,
        String status,
        String assignedBy,
        String createdAt,
        String updatedAt,
        String expiresAt
) {
}
