package com.grab.app.channeldiscovery.saleschannel.internal.api.rest.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AccessContext;
import com.grab.app.channeldiscovery.saleschannel.internal.exception.SalesChannelServiceError;
import com.grab.app.channeldiscovery.saleschannel.internal.exception.SalesChannelServiceException;
import com.grab.app.shared.security.PlatformScopes;
import com.grab.app.shared.security.ScopeResolverHelper;
import com.grab.app.shared.security.SecurityPrincipal;
import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PublishableKeyAccessResolver {
    public PublishableKeyManagementAccess resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            throw forbidden(null);
        }
        boolean administrator = principal.getAuthorities().stream()
                .anyMatch(authority -> SalesChannelAuthorityManifest.KEY_ADMIN.equals(authority.getAuthority()));
        if (administrator) {
            return new PublishableKeyManagementAccess(null, true);
        }
        String merchantId = ScopeResolverHelper.resolveScopeId(principal, PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> forbidden(principal.getAccessContext().orElse(null)));
        Id ownerId = new CommonId(merchantId);
        return new PublishableKeyManagementAccess(ownerId, false);
    }

    private SalesChannelServiceException forbidden(AccessContext context) {
        String scopeKey = context == null ? "UNKNOWN" : context.scopeKey();
        String scopeId = context == null ? "UNKNOWN" : context.scopeId();
        SalesChannelServiceError error = new SalesChannelServiceError.MerchantScopeRequired(scopeKey, scopeId);
        return new SalesChannelServiceException(error, "A merchant account scope is required");
    }
}
