package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response;

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
