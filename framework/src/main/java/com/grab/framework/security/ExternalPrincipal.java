package com.grab.framework.security;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record ExternalPrincipal(
        String issuer,
        String subject,
        Optional<String> email,
        Set<String> entitlements
) {
    public ExternalPrincipal {
        Objects.requireNonNull(issuer, "issuer is required");
        Objects.requireNonNull(subject, "subject is required");
        email = Objects.requireNonNull(email, "email is required");
        entitlements = Set.copyOf(Objects.requireNonNull(entitlements, "entitlements are required"));
    }
}
