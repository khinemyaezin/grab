package com.grab.service.authorizationserver.core.introspection;

import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import com.grab.service.authorizationserver.core.grant.SellerTokenSupport;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SellerTokenIntrospectionProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizations;
    private final AccountIdentityPort account;
    private final SellerIssuerProperties properties;
    private final MeterRegistry metrics;

    public SellerTokenIntrospectionProvider(OAuth2AuthorizationService authorizations,
                                            AccountIdentityPort account,
                                            SellerIssuerProperties properties,
                                            MeterRegistry metrics) {
        this.authorizations = authorizations;
        this.account = account;
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OAuth2TokenIntrospectionAuthenticationToken request = (OAuth2TokenIntrospectionAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken client = SellerTokenSupport.validateClient(request.getPrincipal(), properties.clientId());
        String supplied = request.getToken();
        OAuth2Authorization authorization = authorizations.findByToken(supplied, OAuth2TokenType.ACCESS_TOKEN);
        OAuth2TokenIntrospection.Builder claims = OAuth2TokenIntrospection.builder();
        if (SellerTokenSupport.owns(authorization, client)
                && authorization.getAccessToken() != null
                && authorization.getAccessToken().isActive()) {
            AuthenticationState state = SellerTokenSupport.liveState(authorization, account, metrics);
            if (state != null) {
                OAuth2AccessToken access = authorization.getAccessToken().getToken();
                claims.active(true)
                        .clientId(client.getRegisteredClient().getClientId())
                        .issuer(properties.issuer()).audience(properties.audience())
                        .subject(state.userId()).issuedAt(access.getIssuedAt()).expiresAt(access.getExpiresAt())
                        .tokenType("Bearer").claim("email", state.email())
                        .claim(SellerTokenSupport.VERSION, state.authenticationVersion())
                        .claim("roles", state.roles())
                        .claim("authorities", state.authorities());
                if (state.assignmentId() != null) {
                    claims.claim(SellerTokenSupport.ASSIGNMENT, state.assignmentId())
                            .claim(SellerTokenSupport.SCOPE_KEY, state.scopeKey())
                            .claim(SellerTokenSupport.SCOPE_ID, state.scopeId());
                }
            }
        }
        return new OAuth2TokenIntrospectionAuthenticationToken(supplied, client, claims.build());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenIntrospectionAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
