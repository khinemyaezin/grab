package com.identity.domain.security;

import java.time.Instant;
import java.util.Objects;

public record RoleDeclarationReceipt(
        String eventId,
        String owner,
        String roleCode,
        int revision,
        String contentDigest,
        RoleDeclarationCandidateStatus status,
        Instant receivedAt
) {
    public RoleDeclarationReceipt {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(owner, "owner is required");
        Objects.requireNonNull(roleCode, "roleCode is required");
        Objects.requireNonNull(contentDigest, "contentDigest is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(receivedAt, "receivedAt is required");
    }
}
