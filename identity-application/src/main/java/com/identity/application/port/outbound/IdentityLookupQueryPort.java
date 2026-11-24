package com.identity.application.port.outbound;

import com.grab.framework.id.Id;
import com.grab.framework.security.AccessContext;
import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.model.read.CredentialView;
import com.grab.framework.security.AuthenticatedActor;

import java.util.Optional;
import java.util.Set;

public interface IdentityLookupQueryPort {
    Optional<CredentialView> findCredentials(String normalizedEmail);

    Optional<ActiveSubjectView> findActiveSubject(Id userId);

    Optional<AuthenticatedActor> resolveSeller(Id userId, Id assignmentId);

    Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext);

    Optional<AuthenticatedActor> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContext accessContext
    );
}
