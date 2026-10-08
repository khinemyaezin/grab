package com.grab.store.merchant.internal.api.rest.config;

import com.merchant.application.security.MerchantAuthorityManifest;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
public class MerchantSecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchants").permitAll();

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
