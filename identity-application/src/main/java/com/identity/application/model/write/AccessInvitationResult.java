package com.identity.application.model.write;

public record AccessInvitationResult(
        String id,
        String inviteeEmail,
        String roleCode,
        String scopeKey,
        String scopeId,
        String status,
        String expiresAt,
        String acceptanceToken
) {
}
