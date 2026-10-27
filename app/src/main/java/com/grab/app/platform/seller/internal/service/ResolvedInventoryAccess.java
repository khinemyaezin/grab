package com.grab.app.platform.seller.internal.service;

import java.util.Objects;
import java.util.Set;

public record ResolvedInventoryAccess(
        String actorId,
        String scopeKey,
        String scopeId,
        Set<String> authorities
) {
    public ResolvedInventoryAccess(String actorId, String scopeKey, String scopeId) {
        this(actorId, scopeKey, scopeId, Set.of());
    }

    public ResolvedInventoryAccess {
        Set<String> requiredAuthorities = Objects.requireNonNull(authorities, "authorities are required");
        authorities = Set.copyOf(requiredAuthorities);
    }
}
