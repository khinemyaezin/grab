package com.manifest.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;

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

    protected SecurityManifestPublicationState() {
    }

    protected SecurityManifestPublicationState(String moduleKey) {
        this.moduleKey = Objects.requireNonNull(moduleKey, "moduleKey must not be null");
        this.revision = 0;
        this.digest = "";
    }

    public String moduleKey() { return moduleKey; }
    public Instant lastEnqueuedAt() { return lastEnqueuedAt; }
    public int revision() { return revision; }
    public String digest() { return digest; }
    public Instant nextPublicationAt() { return nextPublicationAt; }

    public void recordEnqueued(int revision, String digest, Instant now, Instant nextPublicationAt) {
        if (revision < this.revision) {
            throw new IllegalStateException("Security publication revision cannot regress from " + this.revision + " to " + revision);
        }
        if (revision == this.revision && this.digest != null && !this.digest.isEmpty() && !this.digest.equals(digest)) {
            throw new IllegalStateException("Security publication digest conflict for revision " + revision);
        }
        this.revision = revision;
        this.digest = digest;
        this.lastEnqueuedAt = now;
        this.nextPublicationAt = nextPublicationAt;
    }
}
