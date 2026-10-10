package com.identity.domain.security;

import com.grab.framework.security.role.RoleDeclaration;

import java.time.Instant;
import java.util.Objects;

public record RoleDeclarationCandidate(
        String eventId,
        RoleDeclaration declaration,
        String contentDigest,
        RoleDeclarationCandidateStatus status,
        String reason,
        Instant receivedAt,
        Instant appliedAt
) {
    public RoleDeclarationCandidate {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(declaration, "declaration is required");
        Objects.requireNonNull(contentDigest, "contentDigest is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(receivedAt, "receivedAt is required");
    }

    public RoleDeclarationCandidate decide(
            RoleDeclarationCandidateStatus nextStatus,
            String nextReason,
            Instant now
    ) {
        Instant nextAppliedAt = nextStatus == RoleDeclarationCandidateStatus.APPLIED ? now : appliedAt;
        return new RoleDeclarationCandidate(eventId, declaration, contentDigest, nextStatus,
                nextReason, receivedAt, nextAppliedAt);
    }
}
