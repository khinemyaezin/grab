package com.grab.framework.security;

import java.util.Objects;

public record AccessContext(
        String platformCode,
        String assignmentId,
        String scopeType,
        String scopeId
) {
    public AccessContext {
        platformCode = requireText(platformCode, "platformCode");
        assignmentId = requireText(assignmentId, "assignmentId");
        scopeType = requireText(scopeType, "scopeType");
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
