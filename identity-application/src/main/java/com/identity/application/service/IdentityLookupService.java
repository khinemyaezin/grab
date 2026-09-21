package com.identity.application.service;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import com.identity.application.port.outbound.IdentityLookupPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class IdentityLookupService implements IdentityLookupUseCase {

    private final IdentityLookupPort identityLookupPort;

    @Override
    public Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext) {
        return identityLookupPort.resolveByPlatformUserId(issuer, userId, accessContext);
    }

    @Override
    public Optional<AuthenticatedActor> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContext accessContext
    ) {
        return identityLookupPort.resolveByExternalIdentity(issuer, subject, entitlements, accessContext);
    }
}
