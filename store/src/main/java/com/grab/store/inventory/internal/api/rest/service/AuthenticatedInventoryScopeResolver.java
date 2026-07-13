package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.ScopeResolverHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedInventoryScopeResolver {
    private static final String UNKNOWN = "UNKNOWN";

    public String resolveOwnerMerchantId(SecurityPrincipal principal) {
        return ScopeResolverHelper.resolveScopeId(
                        principal,
                        PlatformScopes.SELLER_PORTAL,
                        PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> buildForbiddenException(principal));
    }

    public ResolvedInventoryAccess resolve(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null
                || !PlatformScopes.SELLER_PORTAL.equals(context.platformCode())
                || context.scopeId() == null
                || context.scopeId().isBlank()) {
            throw buildForbiddenException(principal);
        }

        boolean inventoryCapable = PlatformScopes.MERCHANT_ACCOUNT_SCOPE.equals(context.scopeKey())
                || PlatformScopes.FULFILLMENT_LOCATION_SCOPE.equals(context.scopeKey());
        if (!inventoryCapable) {
            throw buildForbiddenException(principal);
        }

        return new ResolvedInventoryAccess(
                principal.getPlatformUserId(),
                context.scopeKey(),
                context.scopeId()
        );
    }

    private InventoryServiceException buildForbiddenException(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            return forbidden(UNKNOWN, UNKNOWN, UNKNOWN);
        }
        return forbidden(context.platformCode(), context.scopeKey(), context.scopeId());
    }

    private InventoryServiceException forbidden(String platformCode, String scopeKey, String scopeId) {
        InventoryServiceError error = new InventoryServiceError.InventoryScopeForbidden(
                safeValue(platformCode),
                safeValue(scopeKey),
                safeValue(scopeId)
        );
        return new InventoryServiceException(error);
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
