package com.grab.store.identity.internal.api.rest.dto.response;

public record AccessAssignmentResponse(
        String id,
        String userId,
        String platformCode,
        String roleCode,
        String scopeType,
        String scopeId,
        String status,
        String assignedBy,
        String createdAt,
        String updatedAt,
        String expiresAt
) {
}
