package com.grab.service.authorizationserver.core.userinfo;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity.Identity;
import com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AuthorizationUserInfoService implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {
    private final RegisteredClientRepository registrations;
    private final AuthorizationClientPolicies policies;
    private final AuthorizationIdentity identities;

    public AuthorizationUserInfoService(RegisteredClientRepository registrations, AuthorizationClientPolicies policies,
                                        AuthorizationIdentity identities) {
        this.registrations = registrations;
        this.policies = policies;
        this.identities = identities;
    }

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        RegisteredClient client = registrations.findById(authorization.getRegisteredClientId());
        if (client == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        String clientId = client.getClientId();
        Client policy = policies.find(clientId).orElseThrow();
        if (!AuthorizationTokenSupport.hasAudience(authorization, policy.audience())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        Identity identity = identities.live(authorization);
        if (identity == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        return OidcUserInfo.builder().subject(identity.subject().userId()).build();
    }
}
