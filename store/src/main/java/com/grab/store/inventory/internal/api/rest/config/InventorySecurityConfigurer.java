package com.grab.store.inventory.internal.api.rest.config;

import com.grab.store.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class InventorySecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/inventory/**")
                .hasAuthority("INVENTORY_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/**")
                .hasAuthority("INVENTORY_WRITE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/inventory/**")
                .hasAuthority("INVENTORY_WRITE")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inventory/**")
                .hasAuthority("INVENTORY_WRITE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/inventory/**")
                .hasAuthority("INVENTORY_WRITE");
    }
}
