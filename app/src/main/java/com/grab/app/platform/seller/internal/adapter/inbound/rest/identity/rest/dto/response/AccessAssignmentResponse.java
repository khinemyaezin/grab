package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response;

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
