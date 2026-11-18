package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import com.identity.application.security.IdentityAuthorityManifest;
import com.merchant.application.security.MerchantAuthorityManifest;
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
final class SellerMerchantSecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/merchants",
                        "/api/v1/merchants/root",
                        "/api/v1/merchants/applications/c2c",
                        "/api/v1/merchants/applications/first-party")
                .authenticated();
        access.requestMatchers(HEAD, "/api/v1/merchants",
                        "/api/v1/merchants/root",
                        "/api/v1/merchants/applications/c2c",
                        "/api/v1/merchants/applications/first-party")
                .authenticated();
        access.requestMatchers(POST, "/api/v1/merchants/applications",
                        "/api/v1/merchants/applications/c2c",
                        "/api/v1/merchants/applications/first-party",
                        "/api/v1/merchants/applications/{applicationId}/submit")
                .authenticated();
        access.requestMatchers(GET, "/api/v1/merchants/{merchantId}")
                .hasAuthority(MerchantAuthorityManifest.PROFILE_READ);
        access.requestMatchers(HEAD, "/api/v1/merchants/{merchantId}")
                .hasAuthority(MerchantAuthorityManifest.PROFILE_READ);
        access.requestMatchers(PATCH, "/api/v1/merchants/{merchantId}/profile")
                .hasAuthority(MerchantAuthorityManifest.PROFILE_WRITE);
        access.requestMatchers(GET, "/api/v1/merchants/{merchantId}/members")
                .hasAuthority(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_READ);
        access.requestMatchers(HEAD, "/api/v1/merchants/{merchantId}/members")
                .hasAuthority(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_READ);
        access.requestMatchers(POST, "/api/v1/merchants/{merchantId}/members/invite",
                        "/api/v1/merchants/{merchantId}/members/{memberId}/accept")
                .hasAuthority(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_WRITE);
        access.requestMatchers(PATCH, "/api/v1/merchants/{merchantId}/members/{memberId}/role")
                .hasAuthority(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_WRITE);
        access.requestMatchers(DELETE, "/api/v1/merchants/{merchantId}/members/{memberId}")
                .hasAuthority(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_WRITE);
        access.requestMatchers(GET, "/api/v1/merchants/{merchantId}/storefronts")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_READ);
        access.requestMatchers(HEAD, "/api/v1/merchants/{merchantId}/storefronts")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_READ);
        access.requestMatchers(POST, "/api/v1/merchants/{merchantId}/storefronts")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_WRITE);
        access.requestMatchers(GET, "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_READ);
        access.requestMatchers(HEAD, "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_READ);
        access.requestMatchers(PUT, "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_WRITE);
        access.requestMatchers(PATCH, "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}/activate",
                        "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}/close")
                .hasAuthority(MerchantAuthorityManifest.STOREFRONT_WRITE);
    }
}
