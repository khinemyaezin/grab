package com.grab.store.shared.security.config;

import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.shared.security.ModuleSecurityConfigurer;
import com.grab.store.shared.security.filters.ContextRequiredFilter;
import com.grab.store.shared.security.filters.ProviderBearerAuthenticationFilter;
import com.grab.store.shared.security.util.AuthCookieHelper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class ApiSecurityConfig {
    private final AuthenticationEntryPoint entryPoint;
    private final AccessDeniedHandler deniedHandler;

    public ApiSecurityConfig(
            AuthenticationEntryPoint entryPoint,
            AccessDeniedHandler deniedHandler) {
        this.entryPoint = entryPoint;
        this.deniedHandler = deniedHandler;
    }

    @Bean
    @ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "false")
    public WebSecurityCustomizer securityDisabledCustomizer() {
        return (web) -> web.ignoring().anyRequest();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            List<ModuleSecurityConfigurer> moduleConfigurers,
            @Value("${security.api.core:*}") List<String> allowedOrigins,
            ObjectProvider<PlatformIdentityResolver> resolvers,
            AccessTokenAuthenticator authenticator,
            AuthCookieHelper authCookieHelper
            ) throws Exception {

        ProviderBearerAuthenticationFilter bearerFilter =
                new ProviderBearerAuthenticationFilter(
                        authenticator,
                        resolvers,
                        entryPoint,
                        authCookieHelper
                );
        ContextRequiredFilter contextRequiredFilter = new ContextRequiredFilter();

        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource(allowedOrigins)))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(auth -> {
                    moduleConfigurers.forEach(configurer -> configurer.configure(auth));

                    auth.requestMatchers(HttpMethod.GET,
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**").permitAll();

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
