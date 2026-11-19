package com.grab.service.authorizationserver.core.logout;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;

public class ActiveClientLogoutProvider implements AuthenticationProvider {
    private static final OAuth2TokenType ID_TOKEN = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    private final AuthenticationProvider delegate;
    private final OAuth2AuthorizationService authorizations;
    private final RegisteredClientRepository clients;

    public ActiveClientLogoutProvider(AuthenticationProvider delegate, OAuth2AuthorizationService authorizations,
                                      RegisteredClientRepository clients) {
        this.delegate = delegate;
        this.authorizations = authorizations;
        this.clients = clients;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OidcLogoutAuthenticationToken logout = (OidcLogoutAuthenticationToken) authentication;
        OAuth2Authorization authorization = authorizations.findByToken(logout.getIdTokenHint(), ID_TOKEN);
        if (authorization == null || clients.findById(authorization.getRegisteredClientId()) == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        return delegate.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}
