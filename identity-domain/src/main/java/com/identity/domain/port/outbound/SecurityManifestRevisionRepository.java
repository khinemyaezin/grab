package com.identity.domain.port.outbound;

import com.identity.domain.security.SecurityManifestCandidate;

import java.util.Optional;

public interface SecurityManifestRevisionRepository {
    Optional<SecurityManifestCandidate> find(String moduleKey, int revision);
    int highestAcceptedRevision(String moduleKey);
    void save(SecurityManifestCandidate candidate);
}
