package com.grab.store.identity.internal.command;

public record AccessInvitationResult(
        String id,
        String inviteeEmail,
        String platformCode,
        String roleCode,
        String scopeType,
        String scopeId,
        String status,
        String expiresAt,
        String acceptanceToken
) {
}
