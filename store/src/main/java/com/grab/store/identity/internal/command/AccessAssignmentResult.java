package com.grab.store.identity.internal.command;

import com.identity.domain.aggregate.AccessAssignment;

import java.time.Instant;

public record AccessAssignmentResult(
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
    public static AccessAssignmentResult from(AccessAssignment assignment) {
        return from(assignment, Instant.now());
    }

    public static AccessAssignmentResult from(AccessAssignment assignment, Instant at) {
        return new AccessAssignmentResult(
                assignment.getId().getValue(),
                assignment.getUserId().getValue(),
                assignment.getPlatformCode(),
                assignment.getRoleCode(),
                assignment.getScope().type().name(),
                assignment.getScope().scopeId(),
                assignment.statusAt(at).name(),
                assignment.getAssignedBy() == null ? null : assignment.getAssignedBy().getValue(),
                assignment.getCreatedAt().toString(),
                assignment.getUpdatedAt().toString(),
                assignment.getExpiresAt() == null ? null : assignment.getExpiresAt().toString()
        );
    }
}
