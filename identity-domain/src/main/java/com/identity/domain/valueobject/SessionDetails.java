package com.identity.domain.valueobject;

import com.grab.framework.security.AccessContext;

import java.time.Instant;
import java.util.Optional;

public record SessionDetails(
    String userId,
    String userEmail,
    String tokenFamilyId,
    Instant expiresAt,
    Instant revokedAt,
    Optional<AccessContext> accessContext
) {
    public SessionDetails(
            String userId,
            String userEmail,
            String tokenFamilyId,
            Instant expiresAt,
            Instant revokedAt
    ) {
        this(userId, userEmail, tokenFamilyId, expiresAt, revokedAt, Optional.empty());
    }
}
