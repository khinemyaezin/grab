package com.grab.app.account.identity.port.message;

import java.util.Optional;
import java.util.Set;

/**
 * Universal Java Port Interface for Identity and Actor resolution.
 * Used by perimeter security authenticators without exposing internal identity services.
 */
public interface IdentityLookupPort {

    Optional<AuthenticatedActorResult> resolveByPlatformUserId(
            String issuer,
            String userId,
            AccessContextMessage accessContext
    );

    Optional<AuthenticatedActorResult> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContextMessage accessContext
    );

    record AccessContextMessage(
            String assignmentId,
            String scopeKey,
            String scopeId
    ) {}

    record AuthenticatedActorResult(
            String platformUserId,
            String issuer,
            String subject,
            String email,
            Set<String> roles,
            Set<String> authorities,
            AccessContextMessage accessContext,
            long authenticationVersion
    ) {}
}

