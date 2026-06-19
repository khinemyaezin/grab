package com.grab.store.shared.security;

import com.grab.framework.security.AuthenticatedActor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record SecurityPrincipal(
        AuthenticatedActor actor
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return actor.authorities().stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return actor.email();
    }

    public String getPlatformUserId() {
        return actor.platformUserId();
    }
}
