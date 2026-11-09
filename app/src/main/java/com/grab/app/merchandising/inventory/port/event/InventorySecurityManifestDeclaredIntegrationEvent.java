package com.grab.app.merchandising.inventory.port.event;

import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestDeclaredIntegrationEvent;
import java.time.Instant;

public record InventorySecurityManifestDeclaredIntegrationEvent(SecurityManifest manifest, String eventId,
                                                                String suppliedContentDigest, Instant publishedAt)
        implements SecurityManifestDeclaredIntegrationEvent {
    public InventorySecurityManifestDeclaredIntegrationEvent(SecurityManifest manifest, String eventId) {
        this(manifest, eventId, manifest.contentDigest(), Instant.now());
    }
    @Override public String moduleKey() { return manifest.moduleKey(); }
    @Override public int securityRevision() { return manifest.securityRevision(); }
}

