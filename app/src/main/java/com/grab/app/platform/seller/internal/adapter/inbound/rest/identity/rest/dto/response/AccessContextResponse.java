package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response;

import java.util.Set;

public record AccessContextResponse(
        String assignmentId,
        Set<String> roleCodes,
        String scopeKey,
        String scopeId,
        String expiresAt,
        String title,
        String subtitle
) {
}
