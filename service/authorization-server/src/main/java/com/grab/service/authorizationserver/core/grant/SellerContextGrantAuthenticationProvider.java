package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class SellerContextGrantAuthenticationProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizations;
    private final GrantFamilyStorePort store;
    private final AccountIdentityPort account;
    private final SellerIssuerProperties properties;
    private final OAuth2TokenGenerator<? extends OAuth2Token> generator;
    private final MeterRegistry metrics;

    public SellerContextGrantAuthenticationProvider(OAuth2AuthorizationService authorizations,
                                                    GrantFamilyStorePort store,
                                                    AccountIdentityPort account,
                                                    SellerIssuerProperties properties,
                                                    OAuth2TokenGenerator<? extends OAuth2Token> generator,
                                                    MeterRegistry metrics) {
        this.authorizations = authorizations;
        this.store = store;
        this.account = account;
        this.properties = properties;
        this.generator = generator;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        SellerContextGrant grant = (SellerContextGrant) authentication;
        OAuth2ClientAuthenticationToken client = SellerTokenSupport.validateClient(grant.getPrincipal(), properties.clientId());
        Authentication result = store.inTransaction(() -> {
            OAuth2Authorization authorization = SellerTokenSupport.lockCurrent(
                    authorizations, store, grant.subjectToken(), OAuth2TokenType.ACCESS_TOKEN, client);
            if (authorization == null || !authorization.getAccessToken().isActive()
                    || !authorization.getRefreshToken().isActive()
                    || SellerTokenSupport.liveState(authorization, account, metrics) == null) {
                return null;
            }
            String subject = authorization.getPrincipalName();
            AuthenticationState target = SellerTokenSupport.resolve(account, metrics, subject, grant.assignmentId(), false);
            long expectedVersion = SellerTokenSupport.version(authorization, SellerTokenSupport.VERSION);
            if (target == null || target.authenticationVersion() != expectedVersion) {
                metrics.counter("seller.oidc.context.denied").increment();
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
            }

            RegisteredClient registration = client.getRegisteredClient();
            OAuth2Authorization contextual = SellerTokenSupport.withState(authorization, target);
            Authentication principal = contextual.getAttribute(Principal.class.getName());
            DefaultOAuth2TokenContext.Builder context = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registration)
                    .principal(principal)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .authorization(contextual)
                    .authorizationGrantType(SellerContextGrant.TYPE)
                    .authorizationGrant(grant)
                    .authorizedScopes(contextual.getAuthorizedScopes());
            Jwt jwt = (Jwt) generator.generate(context.tokenType(OAuth2TokenType.ACCESS_TOKEN).build());
            OAuth2RefreshToken refresh = (OAuth2RefreshToken) generator.generate(
                    context.tokenType(OAuth2TokenType.REFRESH_TOKEN).build());
            if (jwt == null || refresh == null) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
            }
            OAuth2AccessToken access = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                    jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), contextual.getAuthorizedScopes());
            OAuth2Authorization next = OAuth2Authorization.from(contextual)
                    .token(access, metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, jwt.getClaims()))
                    .refreshToken(refresh)
                    .build();
            store.consume(authorization.getId(), authorization.getRefreshToken().getToken());
            authorizations.save(next);
            metrics.counter("seller.oidc.context.switched").increment();
            return new OAuth2AccessTokenAuthenticationToken(registration, client, access, refresh);
        });

        if (result == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SellerContextGrant.class.isAssignableFrom(authentication);
    }
}
