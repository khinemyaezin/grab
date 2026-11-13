package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.config;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerModuleSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SellerRootSecurityConfigurer implements SellerModuleSecurityConfigurer {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(
                "/api/v1",
                "/api/v1/",
                "/api/v1/regions/**"
        ).authenticated();
    }
}

