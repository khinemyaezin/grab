package com.grab.store.shared.events.merchant;

import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestDeclaredIntegrationEvent;

import java.util.Objects;
import java.time.Instant;

public record MerchantSecurityManifestDeclaredIntegrationEvent(
        SecurityManifest manifest,
        String eventId,
        String suppliedContentDigest,
        Instant publishedAt
) implements SecurityManifestDeclaredIntegrationEvent {
    public MerchantSecurityManifestDeclaredIntegrationEvent(SecurityManifest manifest, String eventId) {
        this(manifest, eventId, manifest.contentDigest(), Instant.now());
    }
    public MerchantSecurityManifestDeclaredIntegrationEvent {
        Objects.requireNonNull(manifest, "manifest is required");
        Objects.requireNonNull(eventId, "event id is required");
    }

    @Override
    public String moduleKey() {
        return manifest.moduleKey();
    }

    @Override
    public int securityRevision() {
        return manifest.securityRevision();
    }
}
