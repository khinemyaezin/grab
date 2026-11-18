package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import com.catalog.application.security.CatalogAuthorityManifest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.POST;

@Component
final class SellerPricingSecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/pricing",
                        "/api/v1/pricing/preferences/{preferenceId}",
                        "/api/v1/pricing/price-lists",
                        "/api/v1/pricing/price-lists/{priceListId}",
                        "/api/v1/pricing/price-sets/{priceSetId}")
                .hasAuthority(CatalogAuthorityManifest.READ);
        access.requestMatchers(HEAD, "/api/v1/pricing",
                        "/api/v1/pricing/preferences/{preferenceId}",
                        "/api/v1/pricing/price-lists",
                        "/api/v1/pricing/price-lists/{priceListId}",
                        "/api/v1/pricing/price-sets/{priceSetId}")
                .hasAuthority(CatalogAuthorityManifest.READ);
        access.requestMatchers(POST, "/api/v1/pricing/preferences",
                        "/api/v1/pricing/price-lists",
                        "/api/v1/pricing/price-sets",
                        "/api/v1/pricing/price-sets/calculate")
                .hasAuthority(CatalogAuthorityManifest.WRITE);
    }
}
