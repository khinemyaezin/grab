package com.grab.store.saleschannel.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.saleschannel.internal.exception.SalesChannelServiceError;
import com.grab.store.saleschannel.internal.exception.SalesChannelServiceException;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.ScopeResolverHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedSalesChannelMerchantResolver {
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

    public String resolveCurrentMerchantId(SecurityPrincipal principal) {
        return ScopeResolverHelper.resolveScopeId(
                        principal,
                        PlatformScopes.SELLER_PORTAL,
                        PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> buildForbiddenException(principal));
    }

    private SalesChannelServiceException buildForbiddenException(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            return forbidden(UNKNOWN, UNKNOWN);
        }
        return forbidden(context.scopeKey(), context.scopeId());
    }

    private SalesChannelServiceException forbidden(String scopeKey, String scopeId) {
        return new SalesChannelServiceException(
                new SalesChannelServiceError.MerchantScopeRequired(safe(scopeKey), safe(scopeId)),
                "A Seller Portal merchant account scope is required"
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
