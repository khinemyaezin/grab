package com.identity.domain.port.outbound;

import com.grab.framework.security.SecurityManifest;
import com.identity.domain.security.SecurityManifestCandidate;
import com.identity.domain.security.SecurityManifestCandidateStatus;

import java.util.List;
import java.util.Optional;

public interface SecurityManifestRevisionRepository {
    Optional<SecurityManifestCandidate> find(String moduleKey, int revision);

    default int highestAcceptedRevision(String moduleKey) {
        return 0;
    }

    List<SecurityManifestCandidate> findWaiting();

    void record(String eventId, SecurityManifest manifest, SecurityManifestCandidateStatus status, String errorCode);
}
