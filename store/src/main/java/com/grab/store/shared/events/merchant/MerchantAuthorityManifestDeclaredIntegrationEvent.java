package com.grab.store.shared.events.merchant;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;

import java.util.List;

public record MerchantAuthorityManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<AuthorityDefinition> authorities
) implements AuthorityManifestDeclaredIntegrationEvent {
    public MerchantAuthorityManifestDeclaredIntegrationEvent {
        authorities = List.copyOf(authorities);
    }

    @Override
    public String moduleKey() {
        return "merchant";
    }
}
