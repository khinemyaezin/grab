package com.identity.application.model.read;

import java.util.Set;

public record AccessContextResult(
        String assignmentId,
        String platformCode,
        Set<String> roleCodes,
        String scopeKey,
        String scopeId,
        String expiresAt
) {
}
