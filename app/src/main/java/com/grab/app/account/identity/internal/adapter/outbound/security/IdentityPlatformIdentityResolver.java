package com.grab.app.account.identity.internal.adapter.outbound.security;

import com.grab.app.account.identity.internal.adapter.outbound.security.error.IdentityAuthenticationException;
import com.grab.app.account.identity.internal.adapter.outbound.security.error.IdentitySecurityError;
import com.grab.app.account.identity.internal.config.LocalJwtProperties;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import org.springframework.stereotype.Component;

@Component
public class IdentityPlatformIdentityResolver implements PlatformIdentityResolver {

    private final IdentityLookupUseCase identityLookupUseCase;
    private final LocalJwtProperties properties;

    public IdentityPlatformIdentityResolver(IdentityLookupUseCase identityLookupUseCase, LocalJwtProperties properties) {
        this.identityLookupUseCase = identityLookupUseCase;
        this.properties = properties;
    }

    @Override
    public AuthenticatedActor resolve(ExternalPrincipal principal) {
        if (properties.issuer().equals(principal.issuer())) {
            AuthenticatedActor actor = identityLookupUseCase.resolveByPlatformUserId(
                    principal.issuer(),
                    principal.subject(),
                    principal.accessContext()
            ).orElseThrow(this::notLinked);

            if (principal.authenticationVersion() != null
                    && principal.authenticationVersion() != actor.authenticationVersion()) {
                throw new IdentityAuthenticationException(
                        new IdentitySecurityError.AuthenticationSessionExpired(),
                        "Your access changed. Please log in again"
                );
            }
            return actor;
        } else {
            return identityLookupUseCase.resolveByExternalIdentity(
                    principal.issuer(),
                    principal.subject(),
                    principal.entitlements(),
                    principal.accessContext()
            ).orElseThrow(this::notLinked);
        }
    }

    @Override
    public String localIssuer() {
        return properties.issuer();
    }

    private IdentityAuthenticationException notLinked() {
        return new IdentityAuthenticationException(
                new IdentitySecurityError.IdentityNotLinked(),
                "Identity is not linked"
        );
    }
}

