package com.grab.store.shared.events.inventory;

import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.ScopeManifestDeclaredIntegrationEvent;

import java.util.List;

public record InventoryScopeManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<ScopeDeclaration> scopes
) implements ScopeManifestDeclaredIntegrationEvent {
    public InventoryScopeManifestDeclaredIntegrationEvent {
        scopes = List.copyOf(scopes);
    }

    @Override
    public String moduleKey() {
        return "inventory";
    }
}
