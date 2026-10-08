package com.grab.store.saleschannel.internal.api.rest.config;

import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import com.grab.store.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SalesChannelSecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/sales-channels")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/sales-channels/**")
                .hasAuthority(SalesChannelAuthorityManifest.READ);
    }
}
