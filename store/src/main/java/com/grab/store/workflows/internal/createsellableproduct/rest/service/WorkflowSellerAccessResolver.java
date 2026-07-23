package com.grab.store.workflows.internal.createsellableproduct.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.ScopeResolverHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
public class WorkflowSellerAccessResolver {

    private static final String UNKNOWN = "UNKNOWN";

    public record WorkflowAccess(
            String merchantId,
            String actorId,
            String scopeKey,
            String scopeId
    ) {
    }

    public WorkflowAccess resolve(SecurityPrincipal principal) {
        String merchantId = ScopeResolverHelper.resolveScopeId(
                        principal,
                        PlatformScopes.SELLER_PORTAL,
                        PlatformScopes.MERCHANT_ACCOUNT_SCOPE)
                .orElseThrow(() -> forbidden(principal));

        AccessContext context = principal.getAccessContext().orElseThrow(() -> forbidden(principal));
        return new WorkflowAccess(
                merchantId,
                principal.getPlatformUserId(),
                context.scopeKey(),
                context.scopeId()
        );
    }

    private RuntimeException forbidden(SecurityPrincipal principal) {
        AccessContext context = principal != null ? principal.getAccessContext().orElse(null) : null;
        if (context == null) {
            return SharedErrors.workflowScopeForbidden(UNKNOWN, UNKNOWN, UNKNOWN);
        }
        return SharedErrors.workflowScopeForbidden(
                safe(context.platformCode()),
                safe(context.scopeKey()),
                safe(context.scopeId())
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
