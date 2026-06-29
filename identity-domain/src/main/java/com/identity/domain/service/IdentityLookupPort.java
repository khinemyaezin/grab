package com.identity.domain.service;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;

import java.util.Optional;
import java.util.Set;

public interface IdentityLookupPort {
    Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext);

    Optional<AuthenticatedActor> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContext accessContext
    );
}
