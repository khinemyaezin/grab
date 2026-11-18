package com.grab.service.authorizationserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.service.authorizationserver.adapter.inbound.web.LiveIssuerSessionFilter;
import com.grab.service.authorizationserver.adapter.inbound.web.LoginRateLimitFilter;
import com.grab.service.authorizationserver.adapter.inbound.web.AccountFormAuthenticationProvider;
import com.grab.service.authorizationserver.adapter.inbound.web.AuthorizationProtocolFailureHandler;
import com.grab.service.authorizationserver.core.grant.AuthorizationCodeRedeemProvider;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity;
import com.grab.service.authorizationserver.core.logout.ActiveClientLogoutProvider;
import com.grab.service.authorizationserver.core.grant.SellerContextGrantAuthenticationProvider;
import com.grab.service.authorizationserver.core.grant.SellerContextGrantConverter;
import com.grab.service.authorizationserver.core.grant.AuthorizationRefreshTokenProvider;
import com.grab.service.authorizationserver.core.introspection.AuthorizationTokenIntrospectionProvider;
import com.grab.service.authorizationserver.core.revocation.AuthorizationTokenRevocationProvider;
import com.grab.service.authorizationserver.core.userinfo.AuthorizationUserInfoService;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import java.net.URI;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class AuthorizationIssuerSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain issuerProtocol(HttpSecurity http,
                                              SellerContextGrantAuthenticationProvider contextGrantProvider,
                                              AuthorizationTokenIntrospectionProvider introspectionProvider,
                                              AuthorizationTokenRevocationProvider revocationProvider,
                                              AuthorizationUserInfoService userInfoService,
                                              AccountIdentityPort account,
                                              GrantFamilyStorePort store,
                                              OAuth2AuthorizationService authorizations,
                                              AuthorizationIdentity identities,
                                              RegisteredClientRepository clients,
                                              AuthorizationClientPolicies clientPolicies,
                                              MeterRegistry metrics,
                                              OAuth2TokenGenerator<? extends OAuth2Token> generator) throws Exception {
        OAuth2AuthorizationServerConfigurer issuer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        AuthenticationFailureHandler failure = new AuthorizationProtocolFailureHandler();

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
                                                            String redirect = token.getRedirectUri();
                                                            if (!context.getRegisteredClient().getRedirectUris().contains(redirect)) {
                                                                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                                                                        "The redirect URI must exactly match the registration", null);
                                                                throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
                                                            }
                                                            Object method = token.getAdditionalParameters().get("code_challenge_method");
                                                            if (!"S256".equals(method)) {
                                                                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                                                                        "S256 PKCE is required", null);
                                                                throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
                                                            }
                                                        }));
                                    }
                                })))
                        .tokenEndpoint(token -> token
                                .accessTokenRequestConverter(new SellerContextGrantConverter())
                                .authenticationProviders(providers -> {
                                    providers.replaceAll(provider -> {
                                        if (provider instanceof OAuth2AuthorizationCodeAuthenticationProvider) {
                                            return new AuthorizationCodeRedeemProvider(provider, authorizations, store, identities, clientPolicies);
                                        }
                                        if (provider instanceof OAuth2RefreshTokenAuthenticationProvider) {
                                            return new AuthorizationRefreshTokenProvider(provider, authorizations, store, identities, clientPolicies, metrics);
                                        }
                                        return provider;
                                    });
                                    providers.addFirst(contextGrantProvider);
                                })
                                .errorResponseHandler(failure))
                        .tokenIntrospectionEndpoint(endpoint -> endpoint
                                .authenticationProviders(providers -> {
                                    providers.clear();
                                    providers.add(introspectionProvider);
                                })
                                .errorResponseHandler(failure))
                        .tokenRevocationEndpoint(endpoint -> endpoint
                                .authenticationProviders(providers -> {
                                    providers.clear();
                                    providers.add(revocationProvider);
                                })
                                .errorResponseHandler(failure))
                        .oidc(oidc -> oidc
                                .userInfoEndpoint(userInfo -> userInfo.userInfoMapper(userInfoService))
                                .logoutEndpoint(logout -> logout.authenticationProviders(providers -> providers.replaceAll(
                                        provider -> provider instanceof OidcLogoutAuthenticationProvider
                                                ? new ActiveClientLogoutProvider(provider, authorizations, clients) : provider)))))
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                .addFilterAfter(new LiveIssuerSessionFilter(account), SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain issuerLogin(HttpSecurity http,
                                           AccountFormAuthenticationProvider formAuthProvider,
                                           StringRedisTemplate redis,
                                           AuthorizationIssuerProperties properties) throws Exception {
        http.authenticationProvider(formAuthProvider)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/login",
                                "/error",
                                "/css/**",
                                "/images/**",
                                "/favicon.ico",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .permitAll()
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof AuthenticationServiceException) {
                                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login?error");
                            }
                        }))
                .addFilterBefore(
                        new LoginRateLimitFilter(redis, properties.rateLimitMaxAttempts(), properties.rateLimitWindowSeconds()),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(AuthorizationIssuerProperties properties,
                                                                    Environment environment) {
        String issuer = properties.issuer();
        boolean allowLoopback = environment.acceptsProfiles(Profiles.of("dev & !prod"));
        if (issuer == null || !AuthorizationClientPolicies.validUri(issuer, allowLoopback)) {
            throw new IllegalStateException("The issuer URL requires HTTPS or development loopback HTTP");
        }
        URI uri = URI.create(issuer);
        if (uri.getQuery() != null || issuer.endsWith("/")) {
            throw new IllegalStateException("The issuer URL must omit the query and trailing slash");
        }
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessions) {
        return new SpringSessionBackedSessionRegistry<>(sessions);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbc, AuthorizationClientPolicies policies) {
        JdbcRegisteredClientRepository persisted = new JdbcRegisteredClientRepository(jdbc);
        return new ActiveRegisteredClientRepository(persisted, policies);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbc) {
        RegisteredClientRepository clients = new JdbcRegisteredClientRepository(jdbc);
        JdbcOAuth2AuthorizationService authorizations = new JdbcOAuth2AuthorizationService(jdbc, clients);
        ObjectMapper json = new ObjectMapper();
        ClassLoader loader = AuthorizationIssuerSecurityConfig.class.getClassLoader();
        json.registerModules(SecurityJackson2Modules.getModules(loader));
        json.registerModule(new OAuth2AuthorizationServerJackson2Module());
        json.addMixIn(Long.class, AuthorizationLongMixin.class);
        List<String> legacyAudience = List.of("legacy");
        json.addMixIn(legacyAudience.getClass(), LegacyAudienceMixin.class);

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

    private interface AuthorizationLongMixin {}

    private interface LegacyAudienceMixin {}

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbc, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbc, clients);
    }
}
