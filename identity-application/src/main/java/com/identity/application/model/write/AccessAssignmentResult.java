package com.identity.application.model.write;

import com.identity.domain.aggregate.AccessAssignment;

import java.time.Instant;

public record AccessAssignmentResult(
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
    public static AccessAssignmentResult from(AccessAssignment assignment) {
        return from(assignment, Instant.now());
    }

    public static AccessAssignmentResult from(AccessAssignment assignment, Instant at) {
        return new AccessAssignmentResult(
                assignment.getId().getValue(),
                assignment.getUserId().getValue(),
                assignment.getRoleCode(),
                assignment.getScope().key().value(),
                assignment.getScope().scopeId(),
                assignment.statusAt(at).name(),
                assignment.getAssignedBy() == null ? null : assignment.getAssignedBy().getValue(),
                assignment.getCreatedAt().toString(),
                assignment.getUpdatedAt().toString(),
                assignment.getExpiresAt() == null ? null : assignment.getExpiresAt().toString()
        );
    }

    public static AccessAssignmentResult revoked(String userId, String scopeKey, String scopeId) {
        Instant now = Instant.now();
        return new AccessAssignmentResult(
                null,
                userId,
                null,
                scopeKey,
                scopeId,
                "REVOKED",
                null,
                now.toString(),
                now.toString(),
                null
        );
    }
}
