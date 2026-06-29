package com.grab.store.identity.internal.api.rest.config;

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

        auth.requestMatchers("/api/v1/identity/admin/roles/**").hasAuthority("ROLE_MANAGE");
        auth.requestMatchers(HttpMethod.GET, "/api/v1/identity/admin/users/**").hasAuthority("USER_READ");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/admin/users/*/approve").hasAuthority("USER_APPROVE");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/admin/users/*/suspend").hasAuthority("USER_SUSPEND");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/admin/users/*/reactivate").hasAuthority("USER_SUSPEND");
        auth.requestMatchers("/api/v1/identity/access-contexts/**").authenticated();
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/access-invitations/accept").authenticated();
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/access-invitations").hasAuthority("ACCESS_INVITATION_MANAGE");
        auth.requestMatchers(HttpMethod.POST, "/api/v1/identity/access-invitations/*/cancel").hasAuthority("ACCESS_INVITATION_MANAGE");
        auth.requestMatchers("/api/v1/identity/admin/access-assignments/**").hasAuthority("ACCESS_ASSIGNMENT_MANAGE");
    }
}
