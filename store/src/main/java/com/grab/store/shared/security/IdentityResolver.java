package com.grab.store.shared.security;

import com.grab.framework.security.*;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class IdentityResolver implements PlatformIdentityResolver {
    private final IdentityResolverClient resolverClient;
    private final LocalJwtProperties properties;

    @Override
    public AuthenticatedActor resolve(ExternalPrincipal principal) {
        if (properties.issuer().equals(principal.issuer()))
            return resolverClient.resolveByPlatformUser(
                    principal.issuer(),
                    principal.subject(),
                    principal.accessContext()
            ).orElseThrow(this::notLinked);
        else
            return resolverClient.resolveByExternalIdentity(
                    principal.issuer(),
                    principal.subject(),
                    principal.entitlements(),
                    principal.accessContext()
            ).orElseThrow(this::notLinked);
    }

    @Override
    public String localIssuer() {
        return properties.issuer();
    }

    private IdentityAuthenticationException notLinked() {
        return new IdentityAuthenticationException(new IdentitySecurityError.IdentityNotLinked(), "Identity is not linked");
    }
}
