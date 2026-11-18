package com.grab.service.authorizationserver.core.revocation;

import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import com.grab.service.authorizationserver.core.grant.SellerTokenSupport;
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
public class SellerTokenRevocationProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizations;
    private final GrantFamilyStorePort store;
    private final SellerIssuerProperties properties;

    public SellerTokenRevocationProvider(OAuth2AuthorizationService authorizations,
                                         GrantFamilyStorePort store,
                                         SellerIssuerProperties properties) {
        this.authorizations = authorizations;
        this.store = store;
        this.properties = properties;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OAuth2TokenRevocationAuthenticationToken request = (OAuth2TokenRevocationAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken client = SellerTokenSupport.validateClient(request.getPrincipal(), properties.clientId());
        String token = request.getToken();
        return store.inTransaction(() -> {
            OAuth2Authorization authorization = SellerTokenSupport.lockCurrent(
                    authorizations, store, token, OAuth2TokenType.REFRESH_TOKEN, client);
            if (authorization == null) {
                authorization = SellerTokenSupport.lockCurrent(
                        authorizations, store, token, OAuth2TokenType.ACCESS_TOKEN, client);
            }
            if (authorization == null) {
                authorization = SellerTokenSupport.lockConsumedFamily(authorizations, store, token, client);
            }
            if (authorization != null) {
                SellerTokenSupport.invalidate(authorizations, authorization);
            }
            return request;
        });
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenRevocationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
