package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.security.SecurityManifest;

import java.util.Objects;
import java.time.Instant;

public record RegisterSecurityManifestCommand(
        SecurityManifest manifest,
        String eventId,
        String suppliedContentDigest,
        Instant publishedAt
) implements Command<Void> {
    public RegisterSecurityManifestCommand(SecurityManifest manifest, String eventId) {
        this(manifest, eventId, manifest.contentDigest(), null);
    }

    public RegisterSecurityManifestCommand {
        Objects.requireNonNull(manifest, "manifest is required");
        Objects.requireNonNull(eventId, "event id is required");
        if (eventId.isBlank()) {
            throw new IllegalArgumentException("event id must not be blank");
        }
        Objects.requireNonNull(suppliedContentDigest, "supplied content digest is required");
    }
}
