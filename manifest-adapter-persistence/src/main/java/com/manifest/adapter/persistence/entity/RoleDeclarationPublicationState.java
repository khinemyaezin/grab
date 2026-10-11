package com.manifest.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class RoleDeclarationPublicationState {
    @Id
    @Column(name = "publication_key", length = 512, updatable = false)
    private String publicationKey;
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    @Column(name = "declaration_revision", nullable = false)
    private int revision;
    @Column(name = "content_digest", length = 64, nullable = false)
    private String digest;
    @Column(name = "last_enqueued_at")
    private Instant lastEnqueuedAt;
    @Column(name = "lease_until")
    private Instant nextPublicationAt;

    protected RoleDeclarationPublicationState() {
    }

    protected RoleDeclarationPublicationState(String publicationKey) {
        this.publicationKey = Objects.requireNonNull(publicationKey, "publicationKey is required");
        this.digest = "";
    }

    public int revision() {
        return revision;
    }

    public String digest() {
        return digest;
    }

    public Instant nextPublicationAt() {
        return nextPublicationAt;
    }

    public void recordEnqueued(int revision, String digest, Instant now, Instant nextPublicationAt) {
        if (revision < this.revision) {
            throw new IllegalStateException("Role declaration revision cannot regress");
        }
        if (revision == this.revision && !this.digest.isEmpty() && !this.digest.equals(digest)) {
            throw new IllegalStateException("Role declaration digest conflicts at the same revision");
        }
        this.revision = revision;
        this.digest = Objects.requireNonNull(digest, "digest is required");
        this.lastEnqueuedAt = Objects.requireNonNull(now, "now is required");
        this.nextPublicationAt = Objects.requireNonNull(nextPublicationAt, "nextPublicationAt is required");
    }
}
