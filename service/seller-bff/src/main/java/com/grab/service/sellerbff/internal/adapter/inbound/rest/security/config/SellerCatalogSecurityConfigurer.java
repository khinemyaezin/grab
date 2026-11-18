package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import com.catalog.application.security.CatalogAuthorityManifest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Component
final class SellerCatalogSecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/catalog")
                .authenticated();
        access.requestMatchers(HEAD, "/api/v1/catalog")
                .authenticated();
        access.requestMatchers(GET, "/api/v1/products",
                        "/api/v1/products/{productId}",
                        "/api/v1/products/by-slug/{slug}",
                        "/api/v1/products/{productId}/variants/{variantId}")
                .hasAuthority(CatalogAuthorityManifest.READ);
        access.requestMatchers(HEAD, "/api/v1/products",
                        "/api/v1/products/{productId}",
                        "/api/v1/products/by-slug/{slug}",
                        "/api/v1/products/{productId}/variants/{variantId}")
                .hasAuthority(CatalogAuthorityManifest.READ);
        access.requestMatchers(POST, "/api/v1/products")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
        access.requestMatchers(PUT, "/api/v1/products/{productId}")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
        access.requestMatchers(DELETE, "/api/v1/products/{productId}")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
        access.requestMatchers(PATCH, "/api/v1/products/{productId}/status")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
        access.requestMatchers(GET, "/api/v1/categories/tree",
                        "/api/v1/categories/leaf-nodes",
                        "/api/v1/categories/{categoryId}",
                        "/api/v1/categories/{categoryId}/children",
                        "/api/v1/variant-options",
                        "/api/v1/variant-options/types")
                .hasAuthority(CatalogAuthorityManifest.READ);
        access.requestMatchers(HEAD, "/api/v1/categories/tree",
                        "/api/v1/categories/leaf-nodes",
                        "/api/v1/categories/{categoryId}",
                        "/api/v1/categories/{categoryId}/children",
                        "/api/v1/variant-options",
                        "/api/v1/variant-options/types")
                .hasAuthority(CatalogAuthorityManifest.READ);
        access.requestMatchers(POST, "/api/v1/categories")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
        access.requestMatchers(DELETE, "/api/v1/categories/{categoryId}")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
    }
}
