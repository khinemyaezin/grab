package com.grab.service.authorizationserver.core.revocation;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationTokenRevocationProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizations;
    private final GrantFamilyStorePort store;
    private final AuthorizationClientPolicies clients;

    public AuthorizationTokenRevocationProvider(OAuth2AuthorizationService authorizations,
                                         GrantFamilyStorePort store,
                                         AuthorizationClientPolicies clients) {
        this.authorizations = authorizations;
        this.store = store;
        this.clients = clients;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OAuth2TokenRevocationAuthenticationToken request = (OAuth2TokenRevocationAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken client = AuthorizationTokenSupport.validateClient(request.getPrincipal(), clients);
        String token = request.getToken();
        return store.inTransaction(() -> {
            OAuth2Authorization authorization = AuthorizationTokenSupport.lockCurrent(
                    authorizations, store, token, OAuth2TokenType.REFRESH_TOKEN, client);
            if (authorization == null) {
                authorization = AuthorizationTokenSupport.lockCurrent(
                        authorizations, store, token, OAuth2TokenType.ACCESS_TOKEN, client);
            }
            if (authorization == null) {
                authorization = AuthorizationTokenSupport.lockConsumedFamily(authorizations, store, token, client);
            }
            if (authorization != null) {
                AuthorizationTokenSupport.invalidate(authorizations, authorization);
            }
            return request;
        });
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenRevocationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
