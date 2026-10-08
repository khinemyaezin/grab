package com.grab.framework.security;

public interface SecurityManifestPublicationPort {
    PublicationResult enqueue(SecurityManifest manifest);

    enum PublicationResult {
        ENQUEUED, NOT_DUE, SUPERSEDED, CONFLICT
    }
}
