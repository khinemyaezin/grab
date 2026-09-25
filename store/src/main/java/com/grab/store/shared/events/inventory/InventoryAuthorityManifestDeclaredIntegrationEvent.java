package com.grab.store.shared.events.inventory;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;

import java.util.List;

public record InventoryAuthorityManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<AuthorityDefinition> authorities
) implements AuthorityManifestDeclaredIntegrationEvent {
    public InventoryAuthorityManifestDeclaredIntegrationEvent {
        authorities = List.copyOf(authorities);
    }

    @Override
    public String moduleKey() {
        return "inventory";
    }
}
