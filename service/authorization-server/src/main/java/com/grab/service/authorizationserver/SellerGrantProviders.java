package com.grab.service.authorizationserver;

import com.grab.service.authorizationserver.SellerAccountAuthentication.AccountUnavailable;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
final class SellerGrantProviders {
    static final String VERSION = "authentication_version";
    static final String ASSIGNMENT = "assignment_id";
    static final String SCOPE_KEY = "scope_key";
    static final String SCOPE_ID = "scope_id";
    private static final String MERCHANT_REVISION = "merchant_revision";
    private static final String STOREFRONT_REVISION = "storefront_revision";
    private static final OAuth2TokenType CODE = new OAuth2TokenType(OAuth2ParameterNames.CODE);

    private final OAuth2AuthorizationService authorizations;
    private final SellerGrantStore store;
    private final SellerAccountAuthentication account;
    private final SellerIssuerProperties properties;
    private final OAuth2TokenGenerator<? extends OAuth2Token> generator;
    private final MeterRegistry metrics;

    SellerGrantProviders(OAuth2AuthorizationService authorizations, SellerGrantStore store,
                         SellerAccountAuthentication account, SellerIssuerProperties properties,
                         OAuth2TokenGenerator<? extends OAuth2Token> generator, MeterRegistry metrics) {
        this.authorizations = authorizations;
        this.store = store;
        this.account = account;
        this.properties = properties;
        this.generator = generator;
        this.metrics = metrics;
    }

    void configureTokenProviders(List<AuthenticationProvider> providers) {
        providers.replaceAll(provider -> {
            if (provider instanceof OAuth2AuthorizationCodeAuthenticationProvider) {
                return provider(OAuth2AuthorizationCodeAuthenticationToken.class, grant -> redeem(grant, provider));
            }
            if (provider instanceof OAuth2RefreshTokenAuthenticationProvider) {
                return provider(OAuth2RefreshTokenAuthenticationToken.class, grant -> refresh(grant, provider));
            }
            return provider;
        });
        providers.addFirst(provider(SellerContextGrant.class, this::switchContext));
    }

    void configureIntrospection(List<AuthenticationProvider> providers) {
        providers.clear();
        providers.add(provider(OAuth2TokenIntrospectionAuthenticationToken.class, this::introspect));
    }

    void configureRevocation(List<AuthenticationProvider> providers) {
        providers.clear();
        providers.add(provider(OAuth2TokenRevocationAuthenticationToken.class, this::revoke));
    }

    OidcUserInfo userInfo(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        AuthenticationState state = liveState(authorization);
        if (state == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        return OidcUserInfo.builder().subject(state.userId()).build();
    }

    private Authentication redeem(OAuth2AuthorizationCodeAuthenticationToken grant, AuthenticationProvider delegate) {
        OAuth2ClientAuthenticationToken client = client(grant.getPrincipal());
        return activeGrant(() -> {
            OAuth2Authorization authorization = lockCurrent(grant.getCode(), CODE, client);
            if (authorization == null) {
                return null;
            }
            OAuth2Authorization.Token<OAuth2AuthorizationCode> code = authorization.getToken(OAuth2AuthorizationCode.class);
            if (!code.isActive()) {
                invalidate(authorization);
                return null;
            }
            Authentication principal = authorization.getAttribute(Principal.class.getName());
            AuthenticationState state = resolve(authorization.getPrincipalName(), null, true);
            if (state == null || state.authenticationVersion() != SellerFormAuthentication.loginVersion(principal)) {
                return null;
            }
            OAuth2Authorization contextual = withState(authorization, state);
            authorizations.save(contextual);
            return delegate.authenticate(grant);
        });
    }

    private Authentication refresh(OAuth2RefreshTokenAuthenticationToken grant, AuthenticationProvider delegate) {
        OAuth2ClientAuthenticationToken client = client(grant.getPrincipal());
        String token = grant.getRefreshToken();
        return activeGrant(() -> {
            OAuth2Authorization authorization = lockCurrent(token, OAuth2TokenType.REFRESH_TOKEN, client);
            if (authorization == null) {
                revokeReplay(token, client);
                return null;
            }
            OAuth2Authorization.Token<OAuth2RefreshToken> current = authorization.getRefreshToken();
            if (!current.isActive() || liveState(authorization) == null) {
                invalidate(authorization);
                return null;
            }
            Authentication result = delegate.authenticate(grant);
            store.consume(authorization.getId(), current.getToken());
            return result;
        });
    }

    private Authentication switchContext(SellerContextGrant grant) {
        OAuth2ClientAuthenticationToken client = client(grant.getPrincipal());
        return activeGrant(() -> {
            OAuth2Authorization authorization = lockCurrent(grant.subjectToken(), OAuth2TokenType.ACCESS_TOKEN, client);
            if (authorization == null || !authorization.getAccessToken().isActive()
                    || !authorization.getRefreshToken().isActive() || liveState(authorization) == null) {
                return null;
            }
            String subject = authorization.getPrincipalName();
            AuthenticationState target = resolve(subject, grant.assignmentId(), false);
            long expectedVersion = version(authorization, VERSION);
            if (target == null || target.authenticationVersion() != expectedVersion) {
                metrics.counter("seller.oidc.context.denied").increment();
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
            }

            RegisteredClient registration = client.getRegisteredClient();
            OAuth2Authorization contextual = withState(authorization, target);
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
    }

    private Authentication introspect(OAuth2TokenIntrospectionAuthenticationToken request) {
        OAuth2ClientAuthenticationToken client = client(request.getPrincipal());
        String supplied = request.getToken();
        OAuth2Authorization authorization = authorizations.findByToken(supplied, OAuth2TokenType.ACCESS_TOKEN);
        OAuth2TokenIntrospection.Builder claims = OAuth2TokenIntrospection.builder();
        if (owns(authorization, client) && authorization.getAccessToken().isActive()) {
            AuthenticationState state = liveState(authorization);
            if (state != null) {
                OAuth2AccessToken access = authorization.getAccessToken().getToken();
                claims.active(true)
                        .clientId(client.getRegisteredClient().getClientId())
                        .issuer(properties.issuer()).audience(properties.audience())
                        .subject(state.userId()).issuedAt(access.getIssuedAt()).expiresAt(access.getExpiresAt())
                        .tokenType("Bearer").claim("email", state.email())
                        .claim(VERSION, state.authenticationVersion()).claim("roles", state.roles())
                        .claim("authorities", state.authorities());
                if (state.assignmentId() != null) {
                    claims.claim(ASSIGNMENT, state.assignmentId()).claim(SCOPE_KEY, state.scopeKey())
                            .claim(SCOPE_ID, state.scopeId());
                }
            }
        }
        return new OAuth2TokenIntrospectionAuthenticationToken(supplied, client, claims.build());
    }

    private Authentication revoke(OAuth2TokenRevocationAuthenticationToken request) {
        OAuth2ClientAuthenticationToken client = client(request.getPrincipal());
        String token = request.getToken();
        return store.transaction(() -> {
            OAuth2Authorization authorization = lockCurrent(token, OAuth2TokenType.REFRESH_TOKEN, client);
            if (authorization == null) {
                authorization = lockCurrent(token, OAuth2TokenType.ACCESS_TOKEN, client);
            }
            if (authorization == null) {
                authorization = lockConsumedFamily(token, client);
            }
            if (authorization != null) {
                invalidate(authorization);
            }
            return request;
        });
    }

    private void revokeReplay(String token, OAuth2ClientAuthenticationToken client) {
        OAuth2Authorization authorization = lockConsumedFamily(token, client);
        if (authorization != null) {
            invalidate(authorization);
            metrics.counter("seller.oidc.refresh.replay").increment();
        }
    }

    private OAuth2Authorization lockConsumedFamily(String token, OAuth2ClientAuthenticationToken client) {
        String family = store.consumedFamily(token);
        if (family == null || !store.lock(family)) {
            return null;
        }
        OAuth2Authorization current = authorizations.findById(family);
        return owns(current, client) ? current : null;
    }

    private OAuth2Authorization lockCurrent(String token, OAuth2TokenType type, OAuth2ClientAuthenticationToken client) {
        OAuth2Authorization candidate = authorizations.findByToken(token, type);
        if (!owns(candidate, client) || !store.lock(candidate.getId())) {
            return null;
        }
        OAuth2Authorization locked = authorizations.findById(candidate.getId());
        if (!owns(locked, client) || locked.getToken(token) == null) {
            return null;
        }
        return locked;
    }

    private void invalidate(OAuth2Authorization authorization) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        if (authorization.getAccessToken() != null) {
            builder.invalidate(authorization.getAccessToken().getToken());
        }
        if (authorization.getRefreshToken() != null) {
            builder.invalidate(authorization.getRefreshToken().getToken());
        }
        OAuth2Authorization revoked = builder.build();
        authorizations.save(revoked);
    }

    private AuthenticationState liveState(OAuth2Authorization authorization) {
        String assignment = authorization.getAttribute(ASSIGNMENT);
        AuthenticationState state = resolve(authorization.getPrincipalName(), assignment, false);
        if (state == null || state.authenticationVersion() != version(authorization, VERSION)
                || state.merchantRevision() != version(authorization, MERCHANT_REVISION)
                || state.storefrontRevision() != version(authorization, STOREFRONT_REVISION)
                || !Objects.equals(authorization.getAttribute(SCOPE_KEY), state.scopeKey())
                || !Objects.equals(authorization.getAttribute(SCOPE_ID), state.scopeId())) {
            return null;
        }
        return state;
    }

    private AuthenticationState resolve(String subject, String assignment, boolean initial) {
        try {
            return account.resolve(subject, assignment, initial);
        } catch (AccountUnavailable exception) {
            metrics.counter("seller.oidc.account.unavailable").increment();
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }
    }

    private OAuth2Authorization withState(OAuth2Authorization authorization, AuthenticationState state) {
        return OAuth2Authorization.from(authorization)
                .attribute(VERSION, state.authenticationVersion())
                .attribute(MERCHANT_REVISION, state.merchantRevision())
                .attribute(STOREFRONT_REVISION, state.storefrontRevision())
                .attributes(attributes -> {
                    attributes.remove(ASSIGNMENT);
                    attributes.remove(SCOPE_KEY);
                    attributes.remove(SCOPE_ID);
                    if (state.assignmentId() != null) {
                        attributes.put(ASSIGNMENT, state.assignmentId());
                        attributes.put(SCOPE_KEY, state.scopeKey());
                        attributes.put(SCOPE_ID, state.scopeId());
                    }
                }).build();
    }

    private long version(OAuth2Authorization authorization, String key) {
        Number version = authorization.getAttribute(key);
        return version == null ? -1 : version.longValue();
    }

    private OAuth2ClientAuthenticationToken client(Object supplied) {
        if (!(supplied instanceof OAuth2ClientAuthenticationToken client) || !client.isAuthenticated()
                || client.getRegisteredClient() == null
                || !properties.clientId().equals(client.getRegisteredClient().getClientId())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        return client;
    }

    private boolean owns(OAuth2Authorization authorization, OAuth2ClientAuthenticationToken client) {
        RegisteredClient registration = client.getRegisteredClient();
        return authorization != null && registration.getId().equals(authorization.getRegisteredClientId());
    }

    private Authentication activeGrant(Supplier<Authentication> work) {
        Authentication result = store.transaction(work);
        if (result == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        return result;
    }

    private static <T extends Authentication> AuthenticationProvider provider(Class<T> type,
                                                                                Function<T, Authentication> function) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) {
                return function.apply(type.cast(authentication));
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return type.isAssignableFrom(authentication);
            }
        };
    }
}
