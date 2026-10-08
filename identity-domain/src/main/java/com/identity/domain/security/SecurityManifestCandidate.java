package com.identity.domain.security;

import com.grab.framework.security.SecurityManifest;

import java.time.Instant;

public record SecurityManifestCandidate(
        String eventId,
        SecurityManifest manifest,
        SecurityManifestCandidateStatus status,
        String errorCode,
        Instant receivedAt,
        Instant appliedAt
) {
}
