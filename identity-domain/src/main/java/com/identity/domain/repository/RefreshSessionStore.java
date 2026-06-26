package com.identity.domain.repository;

import com.identity.domain.valueobject.RefreshSessionDetails;

import java.time.Instant;
import java.util.Optional;

public interface RefreshSessionStore {
    void saveNewSession(String userId, String tokenHash, String tokenFamilyId, Instant expiresAt);
    
    Optional<RefreshSessionDetails> findByTokenHash(String tokenHash);
    
    void revokeFamily(String tokenFamilyId);
    
    void revokeSession(String tokenHash);

    void revokeAll(String userId);
    
    void replaceSession(String oldTokenHash, String newTokenHash, Instant oldRevokedAt);
}
