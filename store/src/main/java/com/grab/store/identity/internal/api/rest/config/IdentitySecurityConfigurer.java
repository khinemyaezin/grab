package com.grab.store.identity.internal.api.rest.config;

import com.grab.store.identity.internal.config.IdentityAuthorityCodes;
import com.grab.store.shared.security.ModuleSecurityConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class IdentitySecurityConfigurer implements ModuleSecurityConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET,
                        "/api/v1/identity")
                .permitAll();
        auth.requestMatchers(HttpMethod.POST,
                        "/api/v1/identity/auth/register",
                        "/api/v1/identity/auth/login",
                        "/api/v1/identity/auth/refresh")
                .permitAll();

        auth.requestMatchers("/api/v1/identity/profile/**").authenticated();

        auth.requestMatchers(HttpMethod.GET, "/api/v1/identity/admin/roles/**")
                .hasAuthority(IdentityAuthorityCodes.ROLE_READ);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/admin/roles/**")
                .hasAuthority(IdentityAuthorityCodes.ROLE_WRITE);
        auth.requestMatchers(HttpMethod.PUT, "/api/v1/identity/admin/roles/**")
                .hasAuthority(IdentityAuthorityCodes.ROLE_WRITE);
        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/identity/admin/roles/**")
                .hasAuthority(IdentityAuthorityCodes.ROLE_WRITE);

        auth.requestMatchers(HttpMethod.GET, "/api/v1/identity/admin/users/**")
                .hasAuthority(IdentityAuthorityCodes.USER_READ);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/admin/users/**")
                .hasAuthority(IdentityAuthorityCodes.USER_WRITE);

        auth.requestMatchers(HttpMethod.GET, "/api/v1/identity/admin/access-assignments/**")
                .hasAuthority(IdentityAuthorityCodes.ACCESS_ASSIGNMENT_READ);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/admin/access-assignments/**")
                .hasAuthority(IdentityAuthorityCodes.ACCESS_ASSIGNMENT_WRITE);

        auth.requestMatchers("/api/v1/identity/access-contexts/**").authenticated();
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/access-invitations/accept").authenticated();
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/access-invitations")
                .hasAuthority(IdentityAuthorityCodes.ACCESS_INVITATION_WRITE);
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/access-invitations/*/cancel")
                .hasAuthority(IdentityAuthorityCodes.ACCESS_INVITATION_WRITE);
    }
}
