package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import com.inventory.application.security.InventoryAuthorityManifest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

@Component
final class SellerInventorySecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/inventory",
                        "/api/v1/inventory/reorder-suggestions",
                        "/api/v1/inventory/items/{itemId}",
                        "/api/v1/inventory/items/{itemId}/movements/latest",
                        "/api/v1/facilities/locations/{locationId}",
                        "/api/v1/facilities/zones/{zoneId}",
                        "/api/v1/facilities/bins/{binId}")
                .hasAuthority(InventoryAuthorityManifest.READ);
        access.requestMatchers(HEAD, "/api/v1/inventory",
                        "/api/v1/inventory/reorder-suggestions",
                        "/api/v1/inventory/items/{itemId}",
                        "/api/v1/inventory/items/{itemId}/movements/latest",
                        "/api/v1/facilities/locations/{locationId}",
                        "/api/v1/facilities/zones/{zoneId}",
                        "/api/v1/facilities/bins/{binId}")
                .hasAuthority(InventoryAuthorityManifest.READ);
        access.requestMatchers(POST, "/api/v1/inventory/items",
                        "/api/v1/inventory/allocations",
                        "/api/v1/inventory/allocations/deallocate",
                        "/api/v1/facilities/locations",
                        "/api/v1/facilities/locations/{locationId}/zones",
                        "/api/v1/facilities/zones/{zoneId}/bins")
                .hasAuthority(InventoryAuthorityManifest.WRITE);
        access.requestMatchers(PATCH, "/api/v1/facilities/locations/{locationId}/activate",
                        "/api/v1/facilities/locations/{locationId}/deactivate",
                        "/api/v1/facilities/zones/{zoneId}/activate",
                        "/api/v1/facilities/zones/{zoneId}/deactivate",
                        "/api/v1/facilities/bins/{binId}/activate",
                        "/api/v1/facilities/bins/{binId}/deactivate")
                .hasAuthority(InventoryAuthorityManifest.WRITE);
        access.requestMatchers(DELETE, "/api/v1/facilities/locations/{locationId}",
                        "/api/v1/facilities/zones/{zoneId}",
                        "/api/v1/facilities/bins/{binId}")
                .hasAuthority(InventoryAuthorityManifest.WRITE);
    }
}
