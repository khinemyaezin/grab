package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.filters.ProviderBearerAuthenticationFilter;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@EnableScheduling
@EnableRedisHttpSession(redisNamespace = "grab:seller:sessions", maxInactiveIntervalInSeconds = 604800)
@EnableConfigurationProperties(SellerOidcProperties.class)
@ConditionalOnProperty(name = "grab.seller-oidc.enabled", havingValue = "true")
class SellerOidcSecurityConfig {
    private static final Logger LOG = Loggers.getLogger(SellerOidcSecurityConfig.class);
    @Bean
    SecurityFilterChain sellerOidcSecurity(HttpSecurity http, SellerOidcProperties properties,
            ClientRegistrationRepository registrations, SellerOidcBrowserSession sessions, ObjectMapper json,
            ObjectProvider<AccessTokenAuthenticator> authenticator, ObjectProvider<PlatformIdentityResolver> resolver,
            AuthenticationEntryPoint entryPoint, AccessDeniedHandler deniedHandler, SellerAuthCookieHelper cookies,
            @Value("${security.api.cors:http://localhost:3000}") List<String> origins, MeterRegistry metrics) throws Exception {
        HttpSessionOAuth2AuthorizedClientRepository clients = new HttpSessionOAuth2AuthorizedClientRepository();
        DefaultOAuth2AuthorizationRequestResolver requests =
                new DefaultOAuth2AuthorizationRequestResolver(registrations, "/oauth2/authorization");
        requests.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());

        CorsConfiguration cors = new CorsConfiguration();
        List<String> allowedOrigins = origins.stream().filter(origin -> !"*".equals(origin)).toList();
        cors.setAllowedOrigins(allowedOrigins);
        cors.setAllowedMethods(List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-CSRF-TOKEN"));
        cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);

        http.cors(configuration -> configuration.configurationSource(source))
                .csrf(csrf -> csrf.ignoringRequestMatchers(request -> properties.legacyBearer()
                        && request.getHeader(HttpHeaders.AUTHORIZATION) != null
                        && !request.getServletPath().startsWith("/api/v1/identity/auth/")
                        && !request.getServletPath().startsWith("/api/v1/identity/session/contexts/")))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(access -> access
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/oauth2/authorization/seller",
                                "/login/oauth2/code/seller", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1", "/api/v1/identity", "/api/v1/identity/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/identity/auth/register").permitAll()
                        .anyRequest().authenticated())
                .logout(logout -> logout.disable())
                .oauth2Login(login -> login.authorizedClientRepository(clients)
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(requests))
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthorizedClient client = clients.loadAuthorizedClient("seller", authentication, request);
                            try {
                                OidcUser principal = (OidcUser) authentication.getPrincipal();
                                sessions.start(request, client, principal);
                                clients.removeAuthorizedClient("seller", authentication, request, response);
                                cookies.clearTokenCookies().forEach((name, values) ->
                                        values.forEach(value -> response.addHeader(name, value)));
                                response.sendRedirect(properties.ui().toASCIIString());
                            } catch (RuntimeException exception) {
                                metrics.counter("seller.oidc.callback.failed").increment();
                                SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
                                logout.logout(request, response, authentication);
                                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            String code = exception instanceof OAuth2AuthenticationException oauth
                                    ? oauth.getError().getErrorCode() : exception.getClass().getSimpleName();
                            Throwable cause = exception.getCause();
                            boolean unavailable = cause instanceof ResourceAccessException
                                    || (cause instanceof RestClientResponseException httpError
                                    && httpError.getStatusCode().is5xxServerError());
                            metrics.counter("seller.oidc.callback.failed").increment();
                            LOG.warn("Seller OIDC callback rejected code={} unavailable={}", code, unavailable);
                            int status = unavailable ? HttpServletResponse.SC_SERVICE_UNAVAILABLE
                                    : HttpServletResponse.SC_UNAUTHORIZED;
                            response.sendError(status);
                        }));

        SellerOidcBrowserFilter browser = new SellerOidcBrowserFilter(sessions, json);
        if (properties.legacyBearer()) {
            AccessTokenAuthenticator legacy = authenticator.getObject();
            ProviderBearerAuthenticationFilter bearer = new ProviderBearerAuthenticationFilter(
                    legacy, resolver, entryPoint, cookies, false);
            http.addFilterAfter(bearer, OAuth2LoginAuthenticationFilter.class);
            http.addFilterAfter(browser, ProviderBearerAuthenticationFilter.class);
        } else {
            http.addFilterAfter(browser, OAuth2LoginAuthenticationFilter.class);
        }
        return http.build();
    }

    @Bean
    ClientRegistrationRepository sellerRegistration(SellerOidcProperties properties) {
        ClientRegistration client = ClientRegistration.withRegistrationId("seller")
                .issuerUri(properties.issuer())
                .clientId(properties.clientId()).clientSecret(properties.clientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(properties.callback()).scope("openid")
                .authorizationUri(properties.issuer() + "/oauth2/authorize")
                .tokenUri(properties.endpoint("/oauth2/token"))
                .jwkSetUri(properties.endpoint("/oauth2/jwks"))
                .clientName("Seller Central").build();
        return new InMemoryClientRegistrationRepository(client);
    }

    @Bean
    JwtDecoder sellerAccessJwtDecoder(SellerOidcProperties properties) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(client);
        requests.setReadTimeout(Duration.ofSeconds(5));
        RestTemplate rest = new RestTemplate(requests);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.endpoint("/oauth2/jwks"))
                .validateType(false).restOperations(rest).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                jwt -> {
                    boolean valid = "at+jwt".equals(jwt.getHeaders().get("typ")) && jwt.getExpiresAt() != null
                            && jwt.getAudience() != null && jwt.getAudience().contains(properties.audience());
                    if (valid) {
                        return OAuth2TokenValidatorResult.success();
                    }
                    OAuth2Error error = new OAuth2Error("invalid_token");
                    return OAuth2TokenValidatorResult.failure(error);
                }));
        return decoder;
    }

    @Bean
    CookieSerializer sellerOidcCookie(SellerOidcProperties properties) {
        DefaultCookieSerializer cookie = new DefaultCookieSerializer();
        cookie.setCookieName("GRAB_SELLER_SESSION");
        cookie.setCookiePath("/");
        cookie.setUseSecureCookie(properties.secureCookies());
        cookie.setUseHttpOnlyCookie(true);
        cookie.setSameSite("Lax");
        return cookie;
    }

    @Bean
    SellerIssuerClient sellerIssuerClient(SellerOidcProperties properties, JwtDecoder decoder, MeterRegistry metrics) {
        return new SellerIssuerClient(properties, decoder, metrics);
    }

    @Bean
    SellerPendingRevocations sellerPendingRevocations(StringRedisTemplate redis, SellerIssuerClient client, MeterRegistry metrics) {
        return new SellerPendingRevocations(redis, client, metrics);
    }

    @Bean
    SellerOidcBrowserSession sellerBrowserSession(StringRedisTemplate redis, ObjectMapper json,
                                                  SellerIssuerClient issuer, SellerPendingRevocations pending,
                                                  SellerOidcProperties properties) {
        SellerOidcTokenSlots slots = new SellerOidcTokenSlots(redis, json);
        return new SellerOidcBrowserSession(issuer, slots, pending, properties);
    }
}
