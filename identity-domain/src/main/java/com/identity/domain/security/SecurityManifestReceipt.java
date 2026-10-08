package com.identity.domain.security;

import java.util.Objects;

public record SecurityManifestReceipt(
        String eventId,
        String moduleKey,
        int revision,
        String contentDigest,
        SecurityManifestCandidateStatus status,
        String errorCode
) {
    public SecurityManifestReceipt {
        Objects.requireNonNull(eventId, "event id is required");
        Objects.requireNonNull(moduleKey, "module key is required");
        Objects.requireNonNull(contentDigest, "content digest is required");
        Objects.requireNonNull(status, "status is required");
    }
}
