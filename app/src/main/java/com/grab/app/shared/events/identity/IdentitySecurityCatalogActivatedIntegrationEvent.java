package com.grab.app.shared.events.identity;

import com.grab.framework.domain.Event;

public record IdentitySecurityCatalogActivatedIntegrationEvent(long catalogRevision, String moduleKey,
        int securityRevision, String contentDigest) implements Event {
}
