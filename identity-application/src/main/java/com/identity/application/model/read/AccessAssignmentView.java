package com.identity.application.model.read;

import com.identity.domain.enums.AccessAssignmentStatus;

import java.time.Instant;

public record AccessAssignmentView(
        String id,
        String userId,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        AccessAssignmentStatus storedStatus,
        AccessAssignmentStatus effectiveStatus,
        String assignedBy,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt
) {
}
