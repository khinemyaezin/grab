package com.grab.store.identity.internal.api.query;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.query.IdentityLookupQuery;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IdentityLookupQueryAdapter implements IdentityLookupQuery {

    private final IdentityLookupUseCase identityLookupUseCase;

    @Override
    @IdentityReadTransactional
    public Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext) {
        return identityLookupUseCase.resolveByPlatformUserId(issuer, userId, accessContext);
    }

    @Override
    @IdentityReadTransactional
    public Optional<AuthenticatedActor> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContext accessContext
    ) {
        return identityLookupUseCase.resolveByExternalIdentity(issuer, subject, entitlements, accessContext);
    }
}
