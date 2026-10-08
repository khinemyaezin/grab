package com.grab.store.shared.events.merchant;

import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.ScopeManifestDeclaredIntegrationEvent;

import java.util.List;

public record MerchantScopeManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<ScopeDeclaration> scopes
) implements ScopeManifestDeclaredIntegrationEvent {
    public MerchantScopeManifestDeclaredIntegrationEvent {
        scopes = List.copyOf(scopes);
    }

    @Override
    public String moduleKey() {
        return "merchant";
    }
}
