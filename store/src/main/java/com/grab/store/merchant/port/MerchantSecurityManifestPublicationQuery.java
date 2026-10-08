package com.grab.store.merchant.port;

public interface MerchantSecurityManifestPublicationQuery {
    PublicationStatus status();

    record PublicationStatus(String moduleKey, int enqueuedRevision, String contentDigest,
            String lastEnqueuedAt, long pendingCount, String oldestPendingAt) {
    }
}
