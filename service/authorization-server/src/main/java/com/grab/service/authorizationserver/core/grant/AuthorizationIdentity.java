package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.AccountUnavailableException;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;

import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.VERSION;

@Component
public class AuthorizationIdentity {
    private final AccountIdentityPort account;
    private final MeterRegistry metrics;

    public AuthorizationIdentity(AccountIdentityPort account, MeterRegistry metrics) {
        this.account = account;
        this.metrics = metrics;
    }

    public Identity initial(OAuth2Authorization authorization) {
        return resolve(authorization);
    }

    public Identity live(OAuth2Authorization authorization) {
        Identity identity = resolve(authorization);
        if (identity == null || identity.subject().authenticationVersion() != version(authorization)) {
            return null;
        }
        return identity;
    }

    private Identity resolve(OAuth2Authorization authorization) {
        try {
            Subject subject = account.resolve(authorization.getPrincipalName());
            return subject == null ? null : new Identity(subject);
        } catch (AccountUnavailableException exception) {
            metrics.counter("authorization.oidc.account.unavailable").increment();
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }
    }

    public OAuth2Authorization withState(OAuth2Authorization authorization, Identity identity) {
        return OAuth2Authorization.from(authorization)
                .attribute(VERSION, identity.subject().authenticationVersion())
                .build();
    }

    private long version(OAuth2Authorization authorization) {
        Object storedVersion = authorization.getAttribute(VERSION);
        return storedVersion instanceof Number number ? number.longValue() : -1;
    }

    public record Identity(Subject subject) {}
}
