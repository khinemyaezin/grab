package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.ScopeResolverHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
public class AuthenticatedMerchantScopeResolver {
    private static final String UNKNOWN = "UNKNOWN";

    public String resolveCurrentMerchantId(SecurityPrincipal principal) {
        return ScopeResolverHelper.resolveScopeId(
                        principal,
                        PlatformScopes.SELLER_PORTAL,
                        PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> buildForbiddenException(principal));
    }

    public boolean resolveScopedAccess(
            SecurityPrincipal principal,
            String merchantId,
            boolean globalAccess
    ) {
        if (globalAccess) {
            return false;
        }

        AccessContext context = principal.getAccessContext().orElse(null);
        if (context == null) {
            return false;
        }
        if (!PlatformScopes.SELLER_PORTAL.equals(context.platformCode())
                || !PlatformScopes.MERCHANT_ACCOUNT_SCOPE.equals(context.scopeKey())
                || !merchantId.equals(context.scopeId())) {
            throw forbidden(context.platformCode(), context.scopeKey(), context.scopeId());
        }
        return true;
    }

    private MerchantServiceException buildForbiddenException(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            return forbidden(UNKNOWN, UNKNOWN, UNKNOWN);
        }
        return forbidden(context.platformCode(), context.scopeKey(), context.scopeId());
    }

    private MerchantServiceException forbidden(String platformCode, String scopeKey, String scopeId) {
        MerchantServiceError error = new MerchantServiceError.MerchantScopeForbidden(
                safeValue(platformCode),
                safeValue(scopeKey),
                safeValue(scopeId)
        );
        return new MerchantServiceException(error, "A Seller Portal merchant account scope is required");
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
