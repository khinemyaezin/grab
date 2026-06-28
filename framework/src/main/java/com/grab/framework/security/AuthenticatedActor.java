package com.grab.framework.security;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record AuthenticatedActor(
        String platformUserId,
        String issuer,
        String subject,
        String email,
        Set<String> roles,
        Set<String> authorities,
        Optional<AccessContext> accessContext
) {
    public AuthenticatedActor(
            String platformUserId,
            String issuer,
            String subject,
            String email,
            Set<String> roles,
            Set<String> authorities
    ) {
        this(platformUserId, issuer, subject, email, roles, authorities, Optional.empty());
    }

    public AuthenticatedActor {
        Objects.requireNonNull(platformUserId, "platformUserId is required");
        Objects.requireNonNull(issuer, "issuer is required");
        Objects.requireNonNull(subject, "subject is required");
        Objects.requireNonNull(email, "email is required");
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles are required"));
        authorities = Set.copyOf(Objects.requireNonNull(authorities, "authorities are required"));
        accessContext = Objects.requireNonNull(accessContext, "accessContext is required");
    }
}
