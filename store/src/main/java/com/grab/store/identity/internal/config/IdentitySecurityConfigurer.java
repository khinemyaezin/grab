package com.grab.store.identity.internal.config;

import com.grab.store.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class IdentitySecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST,
                "/api/v1/identity/auth/register",
                "/api/v1/identity/auth/login",
                "/api/v1/identity/auth/refresh").permitAll();
    }
}
