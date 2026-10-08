package com.identity.domain.port.outbound;

import com.grab.framework.security.SecurityManifest;
import com.identity.domain.security.SecurityManifestReceipt;

import java.util.Optional;

public interface SecurityManifestInboxRepository {
    Optional<SecurityManifestReceipt> find(String eventId);
    void save(SecurityManifestReceipt receipt);
    void recordConflict(String eventId, SecurityManifest manifest, String suppliedDigest, String errorCode);
}
