package com.grab.store.identity.internal.api.rest.dto.response;

import java.util.Set;

public record AccessContextResponse(
        String assignmentId,
        String platformCode,
        Set<String> roleCodes,
        String scopeKey,
        String scopeId,
        String expiresAt
) {
}
