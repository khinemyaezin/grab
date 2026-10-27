package com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.config;

import com.grab.app.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class CartSecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/carts", "/api/v1/carts/**")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/carts", "/api/v1/carts/**")
                .permitAll();
    }
}
