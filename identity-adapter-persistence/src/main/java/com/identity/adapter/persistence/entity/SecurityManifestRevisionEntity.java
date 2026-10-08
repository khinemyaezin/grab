package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "security_manifest_revision",
        uniqueConstraints = @UniqueConstraint(name = "uk_security_manifest_revision", columnNames = {"module_key", "revision"}))
public class SecurityManifestRevisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "module_key", nullable = false, updatable = false)
    private String moduleKey;

    @Column(nullable = false, updatable = false)
    private int revision;

    @Column(name = "content_digest", nullable = false, updatable = false)
    private String contentDigest;

    @Lob
    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(name = "dependency_error")
    private String dependencyError;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}
