package com.grab.service.authorizationserver.core.user;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

public record IamAccessContext(
        String id,
        String userId,
        String assignmentId,
        String scopeKey,
        String scopeId,
        String role,
        String displayName,
        Set<String> authorities,
        Instant syncedAt
) implements Serializable {
    public IamAccessContext {
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }
}
