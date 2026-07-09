package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedCatalogMerchantResolver {
    private static final String SELLER_PORTAL = "SELLER_PORTAL";
    private static final String MERCHANT_ACCOUNT_SCOPE = "merchant.account";
    private static final String UNKNOWN = "UNKNOWN";

    public String resolveCurrentMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            throw forbidden(UNKNOWN, UNKNOWN);
        }

        AccessContext context = principal.getAccessContext().orElseThrow(() -> forbidden(UNKNOWN, UNKNOWN));
        if (!SELLER_PORTAL.equals(context.platformCode())
                || !MERCHANT_ACCOUNT_SCOPE.equals(context.scopeKey())
                || context.scopeId() == null
                || context.scopeId().isBlank()) {
            throw forbidden(context.scopeKey(), context.scopeId());
        }
        return context.scopeId();
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
