package com.grab.store.identity.internal.api.rest.dto.response;

public record AccessInvitationResponse(
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
