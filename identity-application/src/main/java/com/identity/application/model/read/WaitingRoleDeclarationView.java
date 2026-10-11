package com.identity.application.model.read;

import com.grab.framework.security.role.RoleDeclaration;

import java.time.Instant;

public record WaitingRoleDeclarationView(
        RoleDeclaration declaration,
        String eventId,
        String contentDigest,
        Instant receivedAt
) {
}
