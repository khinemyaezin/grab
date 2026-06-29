package com.grab.store.shared.security;

import com.grab.framework.security.AuthenticatedActor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Optional;
import com.grab.framework.security.AccessContext;

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

    public Optional<AccessContext> getAccessContext() {
        return Optional.of(actor.accessContext());
    }

    public Optional<String> getPlatformCode() {
        return Optional.of(actor.accessContext()).map(AccessContext::platformCode);
    }

    public Optional<String> getScopeKey() {
        return Optional.of(actor.accessContext()).map(AccessContext::scopeKey);
    }

    public Optional<String> getScopeId() {
        return Optional.of(actor.accessContext()).map(AccessContext::scopeId);
    }
}
