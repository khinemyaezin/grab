package com.identity.domain.port.outbound;

import com.identity.domain.security.SecurityManifestCandidateStatus;
import com.identity.domain.security.SecurityManifestReceipt;

import java.util.Optional;

public interface SecurityManifestInboxRepository {
    boolean alreadyProcessed(String eventId);

    default Optional<SecurityManifestReceipt> find(String eventId) {
        return Optional.empty();
    }

    default int appliedRevision(String moduleKey) {
        return 0;
    }

    default Optional<SecurityManifestReceipt> findByModuleRevision(String moduleKey, int revision) {
        return Optional.empty();
    }

    void recordProcessed(String eventId, String moduleKey, int revision, String contentDigest);

    default void recordOutcome(String eventId, String moduleKey, int revision, String contentDigest,
                               SecurityManifestCandidateStatus status, String errorCode) {
        recordProcessed(eventId, moduleKey, revision, contentDigest);
    }
}
