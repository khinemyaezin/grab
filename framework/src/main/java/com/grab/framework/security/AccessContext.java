package com.grab.framework.security;

import java.util.Objects;

public record AccessContext(
        String platformCode,
        String assignmentId,
        String scopeKey,
        String scopeId
) {
    public AccessContext {
        platformCode = requireText(platformCode, "platformCode");
        assignmentId = requireText(assignmentId, "assignmentId");
        scopeKey = requireText(scopeKey, "scopeKey");
        scopeId = requireText(scopeId, "scopeId");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
