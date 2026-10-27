package com.grab.app.channel.saleschannel.port;

public interface SalesChannelSecurityManifestPublicationQuery {
    PublicationStatus status();

    record PublicationStatus(String moduleKey, int enqueuedRevision, String contentDigest,
            String lastEnqueuedAt, long pendingCount, String oldestPendingAt) {
    }
}
