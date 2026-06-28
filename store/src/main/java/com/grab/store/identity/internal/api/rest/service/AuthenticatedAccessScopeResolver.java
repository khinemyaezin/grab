package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.shared.security.SecurityPrincipal;
import com.identity.domain.enums.AccessScopeType;
import com.identity.domain.valueobject.AccessScope;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedAccessScopeResolver {
    public ActorScope resolve(SecurityPrincipal principal) {
        return principal.getAccessContext()
                .map(this::fromContext)
                .orElseGet(() -> globalAdminOrReject(principal));
    }

    private ActorScope fromContext(AccessContext context) {
        return new ActorScope(context.scopeType(), context.scopeId());
    }

    private ActorScope globalAdminOrReject(SecurityPrincipal principal) {
        if (principal.actor().roles().contains("SUPER_ADMIN")) {
            return new ActorScope(AccessScopeType.GLOBAL.name(), AccessScope.GLOBAL_SCOPE_ID);
        }
        throw new IdentityServiceException(
                new IdentityServiceError.AccessScopeForbidden("UNKNOWN", "UNKNOWN"),
                "A scoped access context is required"
        );
    }

    public record ActorScope(String type, String id) {
    }
}
