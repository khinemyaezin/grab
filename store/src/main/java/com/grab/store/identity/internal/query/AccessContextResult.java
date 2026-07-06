package com.grab.store.identity.internal.query;

import java.util.Set;

public record AccessContextResult(
        String assignmentId,
        String platformCode,
        Set<String> roleCodes,
        String scopeKey,
        String scopeId,
        String expiresAt,
        DisplayContext display
) {
    public record DisplayContext(
            String title,
            String status
    ) {
    }
}
