package com.identity.domain.security;

import com.grab.framework.security.SecurityManifest;

import java.time.Instant;
import java.util.Objects;

public record SecurityManifestCandidate(String eventId, SecurityManifest manifest,
        SecurityManifestCandidateStatus status, String errorCode, Instant receivedAt, Instant appliedAt) {
    public SecurityManifestCandidate {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(manifest);
        Objects.requireNonNull(status);
        Objects.requireNonNull(receivedAt);
    }

    public SecurityManifestCandidate decide(SecurityManifestDecision decision, Instant now) {
        if (status != SecurityManifestCandidateStatus.RECEIVED && status != SecurityManifestCandidateStatus.WAITING_DEPENDENCY) {
            return this;
        }
        Instant activatedAt = decision.newlyActivated() ? now : appliedAt;
        var outcome = decision.status();
        String code = decision.errorCode();
        return new SecurityManifestCandidate(eventId, manifest, outcome, code, receivedAt, activatedAt);
    }
}
