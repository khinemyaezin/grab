package com.grab.store.identity.port;

public interface IdentitySecurityManifestPublicationQuery {
    PublicationStatus status();

    record PublicationStatus(String moduleKey, int enqueuedRevision, String contentDigest,
            String lastEnqueuedAt, long pendingCount, String oldestPendingAt) {
    }
}
