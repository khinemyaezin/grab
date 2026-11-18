package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.POST;

@Component
final class SellerSalesChannelSecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/sales-channels",
                        "/api/v1/sales-channels/root",
                        "/api/v1/sales-channels/{salesChannelId}")
                .hasAuthority(SalesChannelAuthorityManifest.READ);
        access.requestMatchers(HEAD, "/api/v1/sales-channels",
                        "/api/v1/sales-channels/root",
                        "/api/v1/sales-channels/{salesChannelId}")
                .hasAuthority(SalesChannelAuthorityManifest.READ);
        access.requestMatchers(POST, "/api/v1/sales-channels/merchant-website")
                .hasAuthority(SalesChannelAuthorityManifest.WRITE);
        access.requestMatchers(GET, "/api/v1/sales-channels/publishable-keys/resolve")
                .hasAuthority(SalesChannelAuthorityManifest.KEY_READ);
        access.requestMatchers(HEAD, "/api/v1/sales-channels/publishable-keys/resolve")
                .hasAuthority(SalesChannelAuthorityManifest.KEY_READ);
        access.requestMatchers(POST, "/api/v1/sales-channels/{channelId}/publishable-keys")
                .hasAuthority(SalesChannelAuthorityManifest.KEY_WRITE);
        access.requestMatchers(DELETE, "/api/v1/sales-channels/{channelId}/publishable-keys/{keyId}")
                .hasAuthority(SalesChannelAuthorityManifest.KEY_WRITE);
    }
}
