package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.Optional;

public record SellerSecurityPrincipal(
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
        return Optional.ofNullable(actor.accessContext());
    }

    public Optional<String> getScopeKey() {
        return getAccessContext().map(AccessContext::scopeKey);
    }

    public Optional<String> getScopeId() {
        return getAccessContext().map(AccessContext::scopeId);
    }

    public String requiredScopeId() {
        return getScopeId()
                .filter(scopeId -> !scopeId.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Authenticated seller has no active merchant scope"));
    }

    public String requiredPlatformUserId() {
        String platformUserId = getPlatformUserId();
        if (platformUserId == null || platformUserId.isBlank()) {
            throw new AccessDeniedException("Authenticated seller has no platform user identity");
        }
        return platformUserId;
    }
}

