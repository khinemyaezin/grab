package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;

@Component
final class SellerRegionSecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/regions",
                        "/api/v1/regions/countries",
                        "/api/v1/regions/{regionId}")
                .authenticated();
        access.requestMatchers(HEAD, "/api/v1/regions",
                        "/api/v1/regions/countries",
                        "/api/v1/regions/{regionId}")
                .authenticated();
    }
}
