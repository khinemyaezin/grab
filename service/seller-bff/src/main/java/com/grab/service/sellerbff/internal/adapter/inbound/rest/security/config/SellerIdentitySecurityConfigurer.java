package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.config;

import com.merchant.application.security.MerchantAuthorityManifest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.POST;

@Component
final class SellerIdentitySecurityConfigurer implements SellerModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry access) {
        access.requestMatchers(GET, "/api/v1/identity")
                .permitAll();
        access.requestMatchers(GET, "/api/v1/identity/auth/csrf")
                .permitAll();
        access.requestMatchers(GET, "/api/v1/identity/auth/register-url")
                .permitAll();
        access.requestMatchers(POST, "/api/v1/identity/auth/register")
                .permitAll();
        access.requestMatchers(POST, "/api/v1/identity/auth/logout")
                .authenticated();
        access.requestMatchers(GET, "/api/v1/identity/auth/logout-url")
                .authenticated();
        access.requestMatchers(GET, "/api/v1/identity/session",
                        "/api/v1/identity/session/contexts",
                        "/api/v1/identity/session/contexts/switch")
                .authenticated();
        access.requestMatchers(HEAD, "/api/v1/identity/session",
                        "/api/v1/identity/session/contexts")
                .authenticated();
        access.requestMatchers(POST, "/api/v1/identity/session/contexts/{assignmentId}/select",
                        "/api/v1/identity/session/contexts/switch")
                .authenticated();
        access.requestMatchers(GET, "/api/v1/identity/admin/merchants")
                .hasAuthority(MerchantAuthorityManifest.GLOBAL_READ);
        access.requestMatchers(HEAD, "/api/v1/identity/admin/merchants")
                .hasAuthority(MerchantAuthorityManifest.GLOBAL_READ);
        access.requestMatchers(POST, "/api/v1/identity/admin/merchants/{merchantId}/request-changes",
                        "/api/v1/identity/admin/merchants/{merchantId}/approve",
                        "/api/v1/identity/admin/merchants/{merchantId}/reject",
                        "/api/v1/identity/admin/merchants/{merchantId}/suspend",
                        "/api/v1/identity/admin/merchants/{merchantId}/reactivate",
                        "/api/v1/identity/admin/merchants/{merchantId}/close")
                .hasAuthority(MerchantAuthorityManifest.LIFECYCLE_WRITE);
    }
}
