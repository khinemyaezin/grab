package com.grab.store.shared.events.merchant;

import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationDeclaredIntegrationEvent;

import java.time.Instant;
import java.util.Objects;

public record MerchantRoleDeclarationDeclaredIntegrationEvent(
        RoleDeclaration declaration,
        String eventId,
        String suppliedContentDigest,
        Instant publishedAt
) implements RoleDeclarationDeclaredIntegrationEvent {
    public MerchantRoleDeclarationDeclaredIntegrationEvent(RoleDeclaration declaration, String eventId) {
        this(declaration, eventId, declaration.contentDigest(), Instant.now());
    }

    public MerchantRoleDeclarationDeclaredIntegrationEvent {
        Objects.requireNonNull(declaration, "declaration is required");
        Objects.requireNonNull(eventId, "eventId is required");
    }

    @Override
    public String owner() {
        return declaration.owner();
    }

    @Override
    public String roleCode() {
        return declaration.roleCode();
    }

    @Override
    public int declarationRevision() {
        return declaration.declarationRevision();
    }
}
