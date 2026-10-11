package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.security.role.RoleDeclaration;

import java.time.Instant;
import java.util.Objects;

public record RegisterRoleDeclarationCommand(
        RoleDeclaration declaration,
        String eventId,
        String suppliedContentDigest,
        Instant publishedAt,
        boolean revalidation
) implements Command<RegisterRoleDeclarationResult> {
    public RegisterRoleDeclarationCommand(RoleDeclaration declaration, String eventId) {
        this(declaration, eventId, declaration.contentDigest(), null, false);
    }

    public RegisterRoleDeclarationCommand(
            RoleDeclaration declaration,
            String eventId,
            String suppliedContentDigest,
            Instant publishedAt
    ) {
        this(declaration, eventId, suppliedContentDigest, publishedAt, false);
    }

    public RegisterRoleDeclarationCommand {
        Objects.requireNonNull(declaration, "declaration is required");
        Objects.requireNonNull(eventId, "event id is required");
        Objects.requireNonNull(suppliedContentDigest, "supplied content digest is required");
    }
}
