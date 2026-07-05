package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
public class AuthenticatedMerchantScopeResolver {
    private static final String SELLER_PORTAL = "SELLER_PORTAL";
    private static final String MERCHANT_ACCOUNT_SCOPE = "merchant.account";
    private static final String UNKNOWN = "UNKNOWN";

    public String resolveCurrentMerchantId(SecurityPrincipal principal) {
        AccessContext context = principal.getAccessContext()
                .orElseThrow(() -> forbidden(UNKNOWN, UNKNOWN, UNKNOWN));

        if (!isMerchantAccountContext(context) || context.scopeId().isBlank()) {
            throw forbidden(context.platformCode(), context.scopeKey(), context.scopeId());
        }
        return context.scopeId();
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
        if (!isMerchantAccountContext(context) || !merchantId.equals(context.scopeId())) {
            throw forbidden(context.platformCode(), context.scopeKey(), context.scopeId());
        }
        return true;
    }

    private boolean isMerchantAccountContext(AccessContext context) {
        return SELLER_PORTAL.equals(context.platformCode())
                && MERCHANT_ACCOUNT_SCOPE.equals(context.scopeKey());
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
