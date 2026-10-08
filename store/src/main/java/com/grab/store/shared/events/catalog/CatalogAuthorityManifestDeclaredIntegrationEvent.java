package com.grab.store.shared.events.catalog;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;

import java.util.List;

public record CatalogAuthorityManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<AuthorityDefinition> authorities
) implements AuthorityManifestDeclaredIntegrationEvent {
    public CatalogAuthorityManifestDeclaredIntegrationEvent {
        authorities = List.copyOf(authorities);
    }

    @Override
    public String moduleKey() {
        return "catalog";
    }
}
