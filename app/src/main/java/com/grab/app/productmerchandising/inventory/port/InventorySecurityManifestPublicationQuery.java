package com.grab.app.productmerchandising.inventory.port;

public interface InventorySecurityManifestPublicationQuery {
    PublicationStatus status();

    record PublicationStatus(String moduleKey, int enqueuedRevision, String contentDigest,
            String lastEnqueuedAt, long pendingCount, String oldestPendingAt) {
    }
}
