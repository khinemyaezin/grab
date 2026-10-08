package com.grab.store.inventory.internal.api.rest.config;

import com.inventory.application.security.InventoryAuthorityManifest;
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
                .hasAuthority(InventoryAuthorityManifest.READ)
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/**")
                .hasAuthority(InventoryAuthorityManifest.WRITE)
                .requestMatchers(HttpMethod.PUT, "/api/v1/inventory/**")
                .hasAuthority(InventoryAuthorityManifest.WRITE)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inventory/**")
                .hasAuthority(InventoryAuthorityManifest.WRITE)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/inventory/**")
                .hasAuthority(InventoryAuthorityManifest.WRITE);
    }
}
