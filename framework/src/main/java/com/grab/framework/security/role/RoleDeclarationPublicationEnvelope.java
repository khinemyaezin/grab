package com.grab.framework.security.role;

import java.time.Instant;
import java.util.Objects;

public record RoleDeclarationPublicationEnvelope(
        RoleDeclaration declaration,
        String eventId,
        String suppliedContentDigest,
        Instant publishedAt
) {
    public RoleDeclarationPublicationEnvelope {
        Objects.requireNonNull(declaration, "declaration is required");
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(suppliedContentDigest, "suppliedContentDigest is required");
        Objects.requireNonNull(publishedAt, "publishedAt is required");
    }
}
