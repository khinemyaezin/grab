package com.identity.domain.valueobject;

import java.time.Instant;

public record SessionDetails(
    String userId,
    String userEmail,
    String tokenFamilyId,
    Instant expiresAt,
    Instant revokedAt
) {
}
