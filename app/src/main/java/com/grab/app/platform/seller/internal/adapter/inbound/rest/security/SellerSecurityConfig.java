package com.grab.app.platform.seller.internal.adapter.inbound.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.filters.ProviderBearerAuthenticationFilter;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.filters.SellerContextRequiredFilter;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.PlatformIdentityResolver;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SellerSecurityConfig {

    private final AuthenticationEntryPoint entryPoint;
    private final AccessDeniedHandler deniedHandler;

    public SellerSecurityConfig(
            AuthenticationEntryPoint entryPoint,
            AccessDeniedHandler deniedHandler) {
        this.entryPoint = entryPoint;
        this.deniedHandler = deniedHandler;
    }

    @Bean
    @ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "false")
    public WebSecurityCustomizer securityDisabledCustomizer() {
        return web -> web.ignoring().anyRequest();
    }

    @Bean
    public SecurityFilterChain sellerSecurityFilterChain(
            HttpSecurity http,
            List<SellerModuleSecurityConfigurer> moduleConfigurers,
            @Value("${security.api.cors:${security.api.cores:${security.api.core:*}}}") List<String> allowedOrigins,
            ObjectProvider<PlatformIdentityResolver> resolvers,
            AccessTokenAuthenticator authenticator,
            SellerAuthCookieHelper authCookieHelper,
            ObjectMapper objectMapper) throws Exception {

        ProviderBearerAuthenticationFilter bearerFilter = new ProviderBearerAuthenticationFilter(
                authenticator,
                resolvers,
                entryPoint,
                authCookieHelper
        );
        SellerContextRequiredFilter contextRequiredFilter = new SellerContextRequiredFilter(objectMapper);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource(allowedOrigins)))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(auth -> {
                    auth.dispatcherTypeMatchers(
                            DispatcherType.ASYNC,
                            DispatcherType.ERROR,
                            DispatcherType.INCLUDE,
                            DispatcherType.FORWARD
                    ).permitAll();

                    moduleConfigurers.forEach(configurer -> configurer.configure(auth));

                    auth.requestMatchers(
                            HttpMethod.GET,
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/v3/api-docs"
                    ).permitAll();

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(bearerFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(contextRequiredFilter, ProviderBearerAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

