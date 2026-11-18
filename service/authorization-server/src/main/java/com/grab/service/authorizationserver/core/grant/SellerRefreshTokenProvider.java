package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;

public class SellerRefreshTokenProvider implements AuthenticationProvider {
    private final AuthenticationProvider delegate;
    private final OAuth2AuthorizationService authorizations;
    private final GrantFamilyStorePort store;
    private final AccountIdentityPort account;
    private final SellerIssuerProperties properties;
    private final MeterRegistry metrics;

    public SellerRefreshTokenProvider(AuthenticationProvider delegate,
                                      OAuth2AuthorizationService authorizations,
                                      GrantFamilyStorePort store,
                                      AccountIdentityPort account,
                                      SellerIssuerProperties properties,
                                      MeterRegistry metrics) {
        this.delegate = delegate;
        this.authorizations = authorizations;
        this.store = store;
        this.account = account;
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OAuth2RefreshTokenAuthenticationToken grant = (OAuth2RefreshTokenAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken client = SellerTokenSupport.validateClient(grant.getPrincipal(), properties.clientId());
        String token = grant.getRefreshToken();
        Authentication result = store.inTransaction(() -> {
            OAuth2Authorization authorization = SellerTokenSupport.lockCurrent(
                    authorizations, store, token, OAuth2TokenType.REFRESH_TOKEN, client);
            if (authorization == null) {
                revokeReplay(token, client);
                return null;
            }
            OAuth2Authorization.Token<OAuth2RefreshToken> current = authorization.getRefreshToken();
            if (current == null || !current.isActive()
                    || SellerTokenSupport.liveState(authorization, account, metrics) == null) {
                SellerTokenSupport.invalidate(authorizations, authorization);
                return null;
            }
            Authentication authResult = delegate.authenticate(grant);
            store.consume(authorization.getId(), current.getToken());
            return authResult;
        });

        if (result == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void revokeReplay(String token, OAuth2ClientAuthenticationToken client) {
        OAuth2Authorization authorization = SellerTokenSupport.lockConsumedFamily(authorizations, store, token, client);
        if (authorization != null) {
            SellerTokenSupport.invalidate(authorizations, authorization);
            metrics.counter("seller.oidc.refresh.replay").increment();
        }
    }
}
