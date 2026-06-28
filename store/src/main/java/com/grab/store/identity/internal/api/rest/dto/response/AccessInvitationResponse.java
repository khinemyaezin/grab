package com.grab.store.identity.internal.api.rest.dto.response;

public record AccessInvitationResponse(
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
