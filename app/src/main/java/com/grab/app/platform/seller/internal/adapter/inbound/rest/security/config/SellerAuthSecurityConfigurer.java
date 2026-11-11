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
        auth.requestMatchers(HttpMethod.GET, "/api/v1/auth").permitAll();
        auth.requestMatchers(HttpMethod.POST,
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/auth/refresh"
        ).permitAll();
        auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated();
    }
}

