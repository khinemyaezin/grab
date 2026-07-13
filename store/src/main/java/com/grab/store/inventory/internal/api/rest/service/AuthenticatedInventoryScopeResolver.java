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

    public boolean resolveLocationAccess(SecurityPrincipal principal, String targetLocationId) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            throw buildForbiddenException(principal);
        }

        if (PlatformScopes.SELLER_PORTAL.equals(context.platformCode())) {
            if (PlatformScopes.MERCHANT_ACCOUNT_SCOPE.equals(context.scopeKey())) {
                if (context.scopeId() != null && !context.scopeId().isBlank()) {
                    return true;
                }
            } else if (PlatformScopes.FULFILLMENT_LOCATION_SCOPE.equals(context.scopeKey())) {
                if (targetLocationId != null && targetLocationId.equals(context.scopeId())) {
                    return true;
                }
            }
        }

        throw buildForbiddenException(principal);
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
