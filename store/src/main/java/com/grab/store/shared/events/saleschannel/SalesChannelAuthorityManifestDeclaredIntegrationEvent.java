package com.grab.store.shared.events.saleschannel;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.AuthorityManifestDeclaredIntegrationEvent;

import java.util.List;

public record SalesChannelAuthorityManifestDeclaredIntegrationEvent(
        int manifestVersion,
        List<AuthorityDefinition> authorities
) implements AuthorityManifestDeclaredIntegrationEvent {
    public SalesChannelAuthorityManifestDeclaredIntegrationEvent {
        authorities = List.copyOf(authorities);
    }

    @Override
    public String moduleKey() {
        return "saleschannel";
    }
}
