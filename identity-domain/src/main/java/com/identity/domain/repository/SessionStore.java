package com.identity.domain.repository;

import com.identity.domain.valueobject.SessionDetails;

import java.time.Instant;
import java.util.Optional;

public interface SessionStore {
    void saveNewSession(String userId, String tokenHash, String tokenFamilyId, Instant expiresAt);
    
    Optional<SessionDetails> findByTokenHash(String tokenHash);
    
    void revokeFamily(String tokenFamilyId);
    
    void revokeSession(String tokenHash);

    void revokeAll(String userId);
    
    void replaceSession(String oldTokenHash, String newTokenHash, Instant oldRevokedAt);
}
