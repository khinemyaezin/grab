package com.identity.domain.repository;

import com.grab.framework.security.AccessContext;
import com.identity.domain.valueobject.SessionDetails;

import java.time.Instant;
import java.util.Optional;

public interface SessionStore {
    default void saveNewSession(String userId, String tokenHash, String tokenFamilyId, Instant expiresAt) {
        saveNewSession(userId, tokenHash, tokenFamilyId, expiresAt, null);
    }

    void saveNewSession(
            String userId,
            String tokenHash,
            String tokenFamilyId,
            Instant expiresAt,
            AccessContext accessContext
    );
    
    Optional<SessionDetails> findByTokenHash(String tokenHash);
    
    void revokeFamily(String tokenFamilyId);
    
    void revokeSession(String tokenHash);

    void revokeAll(String userId);

    void revokeByAssignment(String assignmentId);
    
    void replaceSession(String oldTokenHash, String newTokenHash, Instant oldRevokedAt);
}
