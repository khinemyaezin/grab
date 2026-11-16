package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.security.SecurityManifest;

import java.util.Objects;
import java.time.Instant;

public record RegisterSecurityManifestCommand(
        SecurityManifest manifest,
        String suppliedContentDigest,
        Instant publishedAt
) implements Command<RegisterSecurityManifestResult> {
    public RegisterSecurityManifestCommand(SecurityManifest manifest) {
        this(manifest, manifest.contentDigest(), null);
    }

    public RegisterSecurityManifestCommand {
        Objects.requireNonNull(manifest, "manifest is required");
        Objects.requireNonNull(suppliedContentDigest, "supplied content digest is required");
    }
}
