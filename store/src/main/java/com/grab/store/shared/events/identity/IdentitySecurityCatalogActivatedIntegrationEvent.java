package com.grab.store.shared.events.identity;

import com.grab.framework.domain.Event;

public record IdentitySecurityCatalogActivatedIntegrationEvent(long catalogRevision, String moduleKey,
        int securityRevision, String contentDigest) implements Event {
}
