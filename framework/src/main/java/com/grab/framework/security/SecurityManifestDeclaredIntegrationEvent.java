package com.grab.framework.security;

import com.grab.framework.domain.Event;

import java.time.Instant;

public interface SecurityManifestDeclaredIntegrationEvent extends Event {
    String moduleKey();

    int securityRevision();

    SecurityManifest manifest();

    String eventId();

    default int schemaVersion() {
        return manifest().schemaVersion();
    }

    default String contentDigest() {
        return manifest().contentDigest();
    }

    default String suppliedContentDigest() {
        return contentDigest();
    }

    default Instant publishedAt() {
        return null;
    }
}
