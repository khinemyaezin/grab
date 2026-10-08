package com.grab.store.shared.security;

import java.util.Optional;

public class ScopeResolverHelper {
    
    private ScopeResolverHelper() {
    }

    public static Optional<String> resolveScopeId(SecurityPrincipal principal, String expectedScope) {
        if (principal == null) {
            return Optional.empty();
        }

        return principal.getAccessContext()
                .filter(ctx -> expectedScope.equals(ctx.scopeKey()))
                .map(com.grab.framework.security.AccessContext::scopeId)
                .filter(id -> !id.isBlank());
    }
}