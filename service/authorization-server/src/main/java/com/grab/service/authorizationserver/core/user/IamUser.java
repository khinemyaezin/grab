package com.grab.service.authorizationserver.core.user;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

public record IamUser(
        String id,
        String username,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        boolean enabled,
        String platformUserId,
        long authenticationVersion,
        Set<String> authorities,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
    public IamUser {
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }
}
