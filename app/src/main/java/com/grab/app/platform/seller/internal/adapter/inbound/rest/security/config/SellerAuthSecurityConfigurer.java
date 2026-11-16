package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.config;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthSecurityConfigurer implements SellerModuleSecurityConfigurer {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET,
                "/api/v1/identity",
                "/api/v1/identity/auth"
        ).permitAll();
        auth.requestMatchers(HttpMethod.POST,
                "/api/v1/identity/auth/login",
                "/api/v1/identity/auth/register",
                "/api/v1/identity/auth/refresh",
                "/api/v1/identity/auth/logout"
        ).permitAll();
        auth.requestMatchers(
                "/api/v1/identity/session/**"
        ).authenticated();
    }
}
