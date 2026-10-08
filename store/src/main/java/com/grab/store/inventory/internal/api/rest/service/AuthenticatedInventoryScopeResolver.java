package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.security.InventoryScopeManifest;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.ScopeResolverHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthenticatedInventoryScopeResolver {
    private static final String UNKNOWN = "UNKNOWN";

    public String resolveOwnerMerchantId(SecurityPrincipal principal) {
        return ScopeResolverHelper.resolveScopeId(
                        principal,
                        PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> buildForbiddenException(principal));
    }

    public ResolvedInventoryAccess resolve(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null
                || context.scopeId() == null
                || context.scopeId().isBlank()) {
            throw buildForbiddenException(principal);
        }

        boolean inventoryCapable = InventoryScopeManifest.supports(context.scopeKey());
        if (!inventoryCapable) {
            throw buildForbiddenException(principal);
        }

        Set<String> authorities = authoritiesOf(principal);
        return new ResolvedInventoryAccess(
                principal.getPlatformUserId(),
                context.scopeKey(),
                context.scopeId(),
                authorities
        );
    }

    private Set<String> authoritiesOf(SecurityPrincipal principal) {
        Collection<? extends GrantedAuthority> grantedAuthorities = principal.getAuthorities();
        return grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
    }

    private InventoryServiceException buildForbiddenException(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            return forbidden(UNKNOWN, UNKNOWN);
        }
        return forbidden(context.scopeKey(), context.scopeId());
    }

    private InventoryServiceException forbidden(String scopeKey, String scopeId) {
        InventoryServiceError error = new InventoryServiceError.InventoryScopeForbidden(
                safeValue(scopeKey),
                safeValue(scopeId)
        );
        return new InventoryServiceException(error);
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
