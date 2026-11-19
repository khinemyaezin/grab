package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.VERSION;

@Component
public class AuthorizationIdentity {
    private final IamUserRepository users;
    private final MeterRegistry metrics;

    public AuthorizationIdentity(IamUserRepository users, MeterRegistry metrics) {
        this.users = users;
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
        Optional<IamUser> found = users.findById(authorization.getPrincipalName());
        return found
                .filter(IamUser::enabled)
                .map(user -> new Identity(new Subject(user.id(), user.authenticationVersion())))
                .orElse(null);
    }

    public OAuth2Authorization withState(OAuth2Authorization authorization, Identity identity) {
        return OAuth2Authorization.from(authorization)
                .attribute(VERSION, identity.subject().authenticationVersion())
                .build();
    }

    private static long version(OAuth2Authorization authorization) {
        Object attribute = authorization.getAttribute(VERSION);
        if (attribute instanceof Number number) {
            return number.longValue();
        }
        return -1;
    }

    public record Identity(Subject subject) {}
}
