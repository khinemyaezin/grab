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
        auth.requestMatchers(HttpMethod.GET, "/api/v1/merchant").permitAll();
        auth.requestMatchers("/api/v1/admin/merchants/**").hasAuthority("MERCHANT_REVIEW");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/merchants/applications")
                .hasAuthority("MERCHANT_APPLICATION_CREATE");
        auth.requestMatchers("/api/v1/merchants/**").authenticated();
    }
}
