package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.port.IdentityLookupQuery;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Propagation;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IdentityLookupQueryAdapter implements IdentityLookupQuery {

    private final IdentityLookupUseCase identityLookupUseCase;

    @Override
    @IdentityReadTransactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext) {
        return identityLookupUseCase.resolveByPlatformUserId(issuer, userId, accessContext);
    }

    @Override
    @IdentityReadTransactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<AuthenticatedActor> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContext accessContext
    ) {
        return identityLookupUseCase.resolveByExternalIdentity(issuer, subject, entitlements, accessContext);
    }
}
