package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "security_manifest_inbox")
public class SecurityManifestInboxEntity {
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "module_key", nullable = false, updatable = false)
    private String moduleKey;

    @Column(nullable = false, updatable = false)
    private int revision;

    @Column(name = "content_digest", nullable = false, updatable = false)
    private String contentDigest;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "error_code")
    private String errorCode;
}
