package com.grab.service.contract.event.account.identity;

import com.grab.framework.domain.Event;

public record IdentitySecurityCatalogActivatedIntegrationEvent(
        long catalogRevision,
        String moduleKey,
        int securityRevision,
        String contentDigest
) implements Event {
}
