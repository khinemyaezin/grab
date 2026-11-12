package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.config;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerModuleSecurityConfigurer;
import com.merchant.application.security.MerchantAuthorityManifest;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SellerMerchantSecurityConfigurer implements SellerModuleSecurityConfigurer {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchants/root").permitAll();

        auth.requestMatchers(HttpMethod.GET, "/api/v1/admin/merchants/**")
                .hasAuthority(MerchantAuthorityManifest.GLOBAL_READ);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/admin/merchants/**")
                .hasAuthority(MerchantAuthorityManifest.LIFECYCLE_WRITE);

        auth.requestMatchers(HttpMethod.POST, "/api/v1/merchants/applications/**")
                .authenticated();
        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchants/applications/**")
                .authenticated();
        auth.requestMatchers(HttpMethod.POST, "/api/v1/merchants/accounts/*/submit")
                .authenticated();
        auth.requestMatchers(HttpMethod.PATCH, "/api/v1/merchants/accounts/*/profile")
                .authenticated();

        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchants/storefronts", "/api/v1/merchants/storefronts/**")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_READ);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/merchants/storefronts", "/api/v1/merchants/storefronts/**")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_WRITE);
        auth.requestMatchers(HttpMethod.PATCH, "/api/v1/merchants/storefronts/**")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_WRITE);
        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchants/**")
                .hasAuthority(MerchantAuthorityManifest.PROFILE_READ);
    }
}
