package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config.SellerModuleSecurityConfigurer;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.filters.SellerContextRequiredFilter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

import static com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcRoutes.REGISTRATION_ID;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@EnableConfigurationProperties(SellerOidcSecurityConfig.CorsProperties.class)
class SellerOidcSecurityConfig {
    private static final Logger LOG = Loggers.getLogger(SellerOidcSecurityConfig.class);

    @Bean
    SecurityFilterChain sellerOidcSecurity(HttpSecurity http,
                                           SellerOidcProperties properties,
                                           List<SellerModuleSecurityConfigurer> modules,
                                           DefaultOAuth2AuthorizationRequestResolver requests,
                                           OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> codeClient,
                                           SellerOidcBrowserSession sessions, ObjectMapper json,
                                           AuthenticationEntryPoint entryPoint,
                                           AccessDeniedHandler deniedHandler,
                                           CorsProperties corsProperties,
                                           MeterRegistry metrics
    ) throws Exception {
        HttpSessionOAuth2AuthorizedClientRepository clients = new HttpSessionOAuth2AuthorizedClientRepository();
        UrlBasedCorsConfigurationSource source = sellerCors(corsProperties);

        PathPatternRequestMatcher.Builder matchers = PathPatternRequestMatcher.withDefaults();
        PathPatternRequestMatcher sellerApi = matchers.matcher("/api/v1/**");
        http.cors(configuration -> configuration.configurationSource(source))
                .exceptionHandling(errors ->
                        errors.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(access -> {
                    access.requestMatchers(
                                    "/actuator/health",
                                    "/actuator/health/**",
                                    SellerOidcRoutes.LOGIN,
                                    SellerOidcRoutes.CALLBACK,
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**")
                            .permitAll();
                    modules.forEach(module -> module.configure(access));
                    access.requestMatchers(sellerApi).denyAll()
                            .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                            .anyRequest().authenticated();
                })
                .logout(AbstractHttpConfigurer::disable)
                .oauth2Login(login -> login.authorizedClientRepository(clients)
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(requests))
                        .tokenEndpoint(endpoint -> endpoint.accessTokenResponseClient(codeClient))
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthorizedClient client = clients.loadAuthorizedClient(REGISTRATION_ID, authentication, request);
                            try {
                                OidcUser principal = (OidcUser) authentication.getPrincipal();
                                sessions.start(request, client, principal);
                                clients.removeAuthorizedClient(REGISTRATION_ID, authentication, request, response);
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

        SellerOidcBrowserFilter browser = new SellerOidcBrowserFilter(sessions, json, entryPoint);
        http.addFilterAfter(browser, OAuth2LoginAuthenticationFilter.class);
        SellerContextRequiredFilter context = new SellerContextRequiredFilter();
        http.addFilterBefore(context, AuthorizationFilter.class);
        return http.build();
    }

    static UrlBasedCorsConfigurationSource sellerCors(CorsProperties corsProperties) {
        CorsConfiguration cors = new CorsConfiguration();
        List<String> allowedOrigins = corsProperties.cors().stream().filter(origin -> !"*".equals(origin)).toList();
        cors.setAllowedOrigins(allowedOrigins);
        cors.setAllowedMethods(corsProperties.corsAllowedMethods());
        cors.setAllowedHeaders(corsProperties.corsAllowedHeaders());
        cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @ConfigurationProperties("security.api")
    record CorsProperties(List<String> cors, List<String> corsAllowedMethods, List<String> corsAllowedHeaders) {}
}
