package com.identity.application.model.read;

public record ScopeDisplayView(
        String scopeKey,
        String scopeId,
        String title,
        String subtitle
) {
}
