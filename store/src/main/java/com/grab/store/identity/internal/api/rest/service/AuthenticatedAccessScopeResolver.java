package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.shared.security.SecurityPrincipal;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.ScopeKey;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedAccessScopeResolver {
    public ActorScope resolve(SecurityPrincipal principal) {
        return principal.getAccessContext()
                .map(this::fromContext)
                .orElseThrow(()-> new IdentityServiceException(
                        new IdentityServiceError.AccessScopeForbidden("UNKNOWN", "UNKNOWN"),
                        "A scoped access context is required"
                ));
    }

    private ActorScope fromContext(AccessContext context) {
        return new ActorScope(context.scopeKey(), context.scopeId());
    }

    public record ActorScope(String key, String id) {
    }
}
