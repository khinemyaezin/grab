package com.grab.store.merchant.internal.api.rest.config;

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

        auth.requestMatchers(HttpMethod.GET, "/api/v1/admin/merchants/**").hasAuthority("MERCHANT_GLOBAL_READ");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/admin/merchants/**").hasAuthority("MERCHANT_LIFECYCLE_WRITE");

        auth.requestMatchers(HttpMethod.POST, "/api/v1/merchants/applications/**").hasAuthority("MERCHANT_APPLICATION_WRITE");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/merchants/*/submit").hasAuthority("MERCHANT_APPLICATION_WRITE");
        auth.requestMatchers(HttpMethod.PATCH, "/api/v1/merchants/*/profile").hasAuthority("MERCHANT_PROFILE_WRITE");
        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchants/**").hasAuthority("MERCHANT_PROFILE_READ");
    }
}
