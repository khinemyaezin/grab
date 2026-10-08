package com.grab.outbox.infrastructure.security;

import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;

import java.time.Instant;

public final class PublicationEligibilityPolicy {
    private PublicationEligibilityPolicy() {
    }

    public static PublicationResult decide(SecurityManifest manifest, int revision, String digest,
                                           Instant nextPublicationAt, Instant now) {
        if (manifest.securityRevision() < revision) {
            return PublicationResult.SUPERSEDED;
        }
        String candidateDigest = manifest.contentDigest();
        if (manifest.securityRevision() == revision && !candidateDigest.equals(digest)) {
            return PublicationResult.CONFLICT;
        }
        if (manifest.securityRevision() == revision && nextPublicationAt != null
                && nextPublicationAt.isAfter(now)) {
            return PublicationResult.NOT_DUE;
        }
        return PublicationResult.ENQUEUED;
    }
}
