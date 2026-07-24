package com.grab.store.pricing.internal.api.rest.config;

import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
public class PricingSecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/pricing").permitAll();
    }
}
