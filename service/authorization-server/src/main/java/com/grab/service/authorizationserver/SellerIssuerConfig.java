package com.grab.service.authorizationserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.proc.SecurityContext;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ErrorAuthenticationFailureHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration(proxyBeanMethods = false)
class SellerIssuerConfig {
    @Bean
    @Order(1)
    SecurityFilterChain issuerProtocol(HttpSecurity http, SellerGrantProviders grants, SellerAccountAuthentication account,
                                       OAuth2TokenGenerator<? extends OAuth2Token> generator) throws Exception {
        OAuth2AuthorizationServerConfigurer issuer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        AuthenticationFailureHandler failure = protocolFailure();
        http.securityMatcher(issuer.getEndpointsMatcher())
                .with(issuer, endpoints -> endpoints
                        .tokenGenerator(generator)
                        .authorizationEndpoint(authorization -> authorization.authenticationProviders(providers ->
                                providers.forEach(provider -> {
                                    if (provider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider code) {
                                        code.setAuthenticationValidator(
                                                OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_REDIRECT_URI_VALIDATOR
                                                        .andThen(OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR)
                                                        .andThen(context -> {
                                                            OAuth2AuthorizationCodeRequestAuthenticationToken token = context.getAuthentication();
                                                            Object method = token.getAdditionalParameters().get("code_challenge_method");
                                                            if (!"S256".equals(method)) {
                                                                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
                                                            }
                                                        }));
                                    }
                                })))
                        .tokenEndpoint(token -> token.accessTokenRequestConverter(new SellerContextGrant.Converter())
                                .authenticationProviders(grants::configureTokenProviders).errorResponseHandler(failure))
                        .tokenIntrospectionEndpoint(endpoint -> endpoint
                                .authenticationProviders(grants::configureIntrospection).errorResponseHandler(failure))
                        .tokenRevocationEndpoint(endpoint -> endpoint
                                .authenticationProviders(grants::configureRevocation).errorResponseHandler(failure))
                        .oidc(oidc -> oidc.userInfoEndpoint(userInfo -> userInfo
                                .userInfoMapper(grants::userInfo))))
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                .addFilterAfter(new SellerFormAuthentication.LiveIssuerSession(account), SecurityContextHolderFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain issuerLogin(HttpSecurity http, SellerAccountAuthentication account, StringRedisTemplate redis,
                                    MeterRegistry metrics) throws Exception {
        SellerFormAuthentication authentication = new SellerFormAuthentication(account, metrics);
        http.authenticationProvider(authentication)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.permitAll().failureHandler((request, response, exception) -> {
                    if (exception instanceof AuthenticationServiceException) {
                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login?error");
                    }
                }))
                .addFilterBefore(new SellerFormAuthentication.LoginRateLimit(redis), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(SellerIssuerProperties properties) {
        return AuthorizationServerSettings.builder().issuer(properties.issuer()).build();
    }

    @Bean
    SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessions) {
        return new SpringSessionBackedSessionRegistry<>(sessions);
    }

    @Bean
    @DependsOnDatabaseInitialization
    RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbc, SellerIssuerProperties properties,
                                                           SellerGrantStore store) {
        if (!properties.clientSecretHash().startsWith("{bcrypt}")) {
            throw new IllegalStateException("SELLER_OIDC_CLIENT_SECRET_HASH must contain a delegated BCrypt hash");
        }
        JdbcRegisteredClientRepository clients = new JdbcRegisteredClientRepository(jdbc);
        store.transaction(() -> {
            jdbc.execute("SELECT pg_advisory_xact_lock(6021278)");
            RegisteredClient previous = clients.findByClientId(properties.clientId());
            RegisteredClient.Builder registration = previous == null
                    ? RegisteredClient.withId("grab-seller-browser") : RegisteredClient.from(previous);
            RegisteredClient seller = registration.clientId(properties.clientId())
                    .clientSecret(properties.clientSecretHash())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(SellerContextGrant.TYPE)
                    .redirectUris(uris -> {
                        uris.clear();
                        uris.add(properties.redirectUri());
                    })
                    .postLogoutRedirectUris(uris -> {
                        uris.clear();
                        uris.add(properties.postLogoutRedirectUri());
                    })
                    .scope(OidcScopes.OPENID)
                    .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                    .tokenSettings(TokenSettings.builder().reuseRefreshTokens(false)
                            .accessTokenTimeToLive(properties.accessTokenTtl())
                            .refreshTokenTimeToLive(properties.refreshTokenTtl()).build())
                    .build();
            clients.save(seller);
            return null;
        });
        return clients;
    }

    @Bean
    OAuth2AuthorizationService authorizationService(JdbcTemplate jdbc, RegisteredClientRepository clients) {
        JdbcOAuth2AuthorizationService authorizations = new JdbcOAuth2AuthorizationService(jdbc, clients);
        ObjectMapper json = new ObjectMapper();
        ClassLoader loader = SellerIssuerConfig.class.getClassLoader();
        json.registerModules(SecurityJackson2Modules.getModules(loader));
        json.registerModule(new OAuth2AuthorizationServerJackson2Module());
        json.addMixIn(Long.class, AuthorizationVersionMixin.class);
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rows =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(clients);
        rows.setObjectMapper(json);
        authorizations.setAuthorizationRowMapper(rows);
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper parameters =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper();
        parameters.setObjectMapper(json);
        authorizations.setAuthorizationParametersMapper(parameters);
        return authorizations;
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbc, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbc, clients);
    }

    @Bean
    SellerSigningKeys sellerSigningKeys(SellerIssuerProperties properties) throws GeneralSecurityException, IOException {
        return SellerSigningKeys.load(properties);
    }

    @Bean
    JWKSource<SecurityContext> jwks(SellerSigningKeys signingKeys) {
        return signingKeys.source();
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwks, SellerIssuerProperties properties) {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        JOSEObjectType accessType = new JOSEObjectType("at+jwt");
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(accessType));
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwks));
        processor.setJWTClaimsSetVerifier((claims, context) -> {});
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(processor);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), jwt -> {
                    boolean match = jwt.getExpiresAt() != null && jwt.getAudience() != null
                            && jwt.getAudience().contains(properties.audience());
                    if (match) {
                        return OAuth2TokenValidatorResult.success();
                    }
                    OAuth2Error invalid = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN);
                    return OAuth2TokenValidatorResult.failure(invalid);
                }));
        return decoder;
    }

    @Bean
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(SellerSigningKeys keys, SellerIssuerProperties properties) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(keys.source());
        JwtGenerator jwt = new JwtGenerator(encoder);
        jwt.setJwtCustomizer(context -> {
            context.getJwsHeader().keyId(keys.currentId());
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                OAuth2Authorization authorization = context.getAuthorization();
                context.getJwsHeader().type("at+jwt");
                context.getClaims().audience(Collections.singletonList(properties.audience()));
                context.getClaims().claims(claims -> {
                    claims.put(SellerGrantProviders.VERSION, authorization.getAttribute(SellerGrantProviders.VERSION));
                    String assignment = authorization.getAttribute(SellerGrantProviders.ASSIGNMENT);
                    if (assignment != null) {
                        claims.put(SellerGrantProviders.ASSIGNMENT, assignment);
                        claims.put(SellerGrantProviders.SCOPE_KEY, authorization.getAttribute(SellerGrantProviders.SCOPE_KEY));
                        claims.put(SellerGrantProviders.SCOPE_ID, authorization.getAttribute(SellerGrantProviders.SCOPE_ID));
                    }
                });
            } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())
                    && AuthorizationGrantType.REFRESH_TOKEN.equals(context.getAuthorizationGrantType())) {
                OAuth2Authorization.Token<OidcIdToken> previous = context.getAuthorization().getToken(OidcIdToken.class);
                if (previous != null) {
                    String sid = previous.getToken().getClaimAsString("sid");
                    if (sid != null) {
                        context.getClaims().claim("sid", sid);
                    }
                }
            }
        });
        return new DelegatingOAuth2TokenGenerator(jwt, new OAuth2AccessTokenGenerator(), new OAuth2RefreshTokenGenerator());
    }

    private AuthenticationFailureHandler protocolFailure() {
        AuthenticationFailureHandler defaults = new OAuth2ErrorAuthenticationFailureHandler();
        return (request, response, exception) -> {
            if (exception instanceof OAuth2AuthenticationException protocol
                    && OAuth2ErrorCodes.SERVER_ERROR.equals(protocol.getError().getErrorCode())) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"server_error\"}");
            } else {
                defaults.onAuthenticationFailure(request, response, exception);
            }
        };
    }
    private abstract static class AuthorizationVersionMixin {}

}
