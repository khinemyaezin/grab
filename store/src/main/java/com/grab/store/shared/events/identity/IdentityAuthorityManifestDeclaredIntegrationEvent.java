package com.grab.store.shared.events.identity;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;

import java.util.List;

public record IdentityAuthorityManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<AuthorityDefinition> authorities
) implements AuthorityManifestDeclaredIntegrationEvent {
    public IdentityAuthorityManifestDeclaredIntegrationEvent {
        authorities = List.copyOf(authorities);
    }

    @Override
    public String moduleKey() {
        return "identity";
    }
}
