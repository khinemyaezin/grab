package com.grab.framework.security;

public interface SecurityManifestPublicationQueryPort {
    PublicationStatus status(String moduleKey);

    record PublicationStatus(String moduleKey, int enqueuedRevision, String contentDigest,
            String lastEnqueuedAt, long pendingCount, String oldestPendingAt) {
    }
}
