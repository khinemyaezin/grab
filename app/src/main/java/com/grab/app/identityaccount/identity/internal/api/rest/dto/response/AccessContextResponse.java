package com.grab.app.identityaccount.identity.internal.api.rest.dto.response;

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
