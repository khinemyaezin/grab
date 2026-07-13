package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.ScopeResolverHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedCatalogMerchantResolver {
    private static final String UNKNOWN = "UNKNOWN";

    public String resolveCurrentMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            throw forbidden(UNKNOWN, UNKNOWN);
        }

        return ScopeResolverHelper.resolveScopeId(
                        principal,
                        PlatformScopes.SELLER_PORTAL,
                        PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> buildForbiddenException(principal));
    }

    private CatalogServiceException buildForbiddenException(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            return forbidden(UNKNOWN, UNKNOWN);
        }
        return forbidden(context.scopeKey(), context.scopeId());
    }

    private CatalogServiceException forbidden(String scopeKey, String scopeId) {
        CatalogServiceError error = new CatalogServiceError.MerchantScopeRequired(
                safeValue(scopeKey), safeValue(scopeId));
        return new CatalogServiceException(error, "A Seller Portal merchant account scope is required");
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
