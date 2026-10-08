package com.grab.framework.security;

import java.time.Instant;
import java.util.Objects;

public record SecurityManifestEnvelope(
        String eventType,
        int eventVersion,
        String producer,
        String eventId,
        Instant publishedAt,
        String suppliedContentDigest,
        SecurityManifest manifest
) {
    public static final String TYPE = "security.manifest.declared";
    public static final int VERSION = 1;

    public SecurityManifestEnvelope {
        Objects.requireNonNull(eventId, "event id is required");
        Objects.requireNonNull(publishedAt, "publication time is required");
        Objects.requireNonNull(suppliedContentDigest, "digest is required");
        Objects.requireNonNull(manifest, "manifest is required");
        if (!TYPE.equals(eventType) || eventVersion != VERSION || !manifest.moduleKey().equals(producer)) {
            throw new IllegalArgumentException("Invalid security manifest envelope");
        }
    }
}
