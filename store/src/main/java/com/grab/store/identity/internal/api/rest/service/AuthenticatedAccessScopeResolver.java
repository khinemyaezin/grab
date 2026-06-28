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
                .orElseGet(() -> globalAdminOrReject(principal));
    }

    private ActorScope fromContext(AccessContext context) {
        return new ActorScope(context.scopeKey(), context.scopeId());
    }

    private ActorScope globalAdminOrReject(SecurityPrincipal principal) {
        if (principal.actor().roles().contains("SUPER_ADMIN")) {
            return new ActorScope(ScopeKey.GLOBAL_VALUE, AccessScope.GLOBAL_SCOPE_ID);
        }
        throw new IdentityServiceException(
                new IdentityServiceError.AccessScopeForbidden("UNKNOWN", "UNKNOWN"),
                "A scoped access context is required"
        );
    }

    public record ActorScope(String key, String id) {
    }
}
