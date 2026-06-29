package com.grab.store.shared.security.adapter;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.store.shared.security.IdentityResolverClient;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.IdentityLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IdentityLookupAdapter implements IdentityResolverClient {
    private final IdentityLookupPort resolver;

    @Override
    @Transactional(transactionManager = "identityTransactionManager", readOnly = true)
    public Optional<AuthenticatedActor> resolveByPlatformUser(String issuer, String userId, AccessContext ctx) {
        try {
            return resolver.resolveByPlatformUserId(issuer, userId, ctx);
        } catch (IdentityDomainValidationException ex) {
            throw translateException(ex);
        }
    }

    @Override
    public Optional<AuthenticatedActor> resolveByExternalIdentity(String issuer, String subject, Set<String> entitlements, AccessContext ctx) {
        try {
            return resolver.resolveByExternalIdentity(issuer, subject, entitlements, ctx);
        } catch (IdentityDomainValidationException ex) {
            throw translateException(ex);
        }
    }

    private RuntimeException translateException(IdentityDomainValidationException ex) {
        return switch (ex.getMessageSource().code()) {
            case "idt.domain.user.account_not_active" -> new IdentityAuthenticationException(
                    new IdentitySecurityError.AccountNotActive(),
                    "Account is not active",
                    ex);
            case "idt.domain.access.code_invalid" -> new IdentityAuthenticationException(
                    new IdentitySecurityError.InvalidAccessContext(),
                    "Access context is not active",
                    ex
            );
            default -> ex;
        };
    }
}
