package com.grab.store.shared.security;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;

import java.util.Optional;
import java.util.Set;

public interface IdentityResolverClient {
    Optional<AuthenticatedActor> resolveByPlatformUser(String issuer, String userId, AccessContext ctx);
    Optional<AuthenticatedActor> resolveByExternalIdentity(String issuer, String subject,
                                                 Set<String> entitlements, AccessContext ctx);
}
