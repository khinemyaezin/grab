package com.manifest.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import java.time.Instant;

@MappedSuperclass
public abstract class SecurityManifestPublicationState {
    @Id
    @Column(name = "module_key", updatable = false)
    private String moduleKey;
    @Column(name = "security_revision", nullable = false)
    private int revision;
    @Column(name = "content_digest", nullable = false)
    private String digest;
    @Column(name = "last_enqueued_at")
    private Instant lastEnqueuedAt;
    @Column(name = "lease_until")
    private Instant nextPublicationAt;
    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    public Instant lastEnqueuedAt() { return lastEnqueuedAt; }
    public int revision() { return revision; }
    public String digest() { return digest; }
    public Instant nextPublicationAt() { return nextPublicationAt; }

    public void recordEnqueued(int revision, String digest, Instant now, Instant nextPublicationAt) {
        this.revision = revision;
        this.digest = digest;
        this.lastEnqueuedAt = now;
        this.nextPublicationAt = nextPublicationAt;
    }
}
