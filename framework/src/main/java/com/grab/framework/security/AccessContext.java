package com.grab.framework.security;

public record AccessContext(
        String assignmentId,
        String scopeKey,
        String scopeId
) {
}
