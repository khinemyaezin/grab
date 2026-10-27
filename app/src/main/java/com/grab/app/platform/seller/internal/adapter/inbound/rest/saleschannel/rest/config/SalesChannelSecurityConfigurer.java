package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.config;

import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import com.grab.app.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SalesChannelSecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/sales-channels/channels/*/publishable-keys/**")
                .hasAnyAuthority(SalesChannelAuthorityManifest.KEY_READ, SalesChannelAuthorityManifest.KEY_ADMIN);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/sales-channels/channels/*/publishable-keys")
                .hasAnyAuthority(SalesChannelAuthorityManifest.KEY_WRITE, SalesChannelAuthorityManifest.KEY_ADMIN);
        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/sales-channels/channels/*/publishable-keys/*")
                .hasAnyAuthority(SalesChannelAuthorityManifest.KEY_WRITE, SalesChannelAuthorityManifest.KEY_ADMIN);
        auth.requestMatchers(HttpMethod.GET, "/api/v1/sales-channels")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/sales-channels/**")
                .hasAuthority(SalesChannelAuthorityManifest.READ);
    }
}
