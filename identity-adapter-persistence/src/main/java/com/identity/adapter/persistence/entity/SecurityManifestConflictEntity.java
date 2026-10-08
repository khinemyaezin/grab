package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "security_manifest_conflict")
public class SecurityManifestConflictEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "module_key", nullable = false)
    private String moduleKey;

    @Column(nullable = false)
    private int revision;

    @Column(name = "content_digest", nullable = false)
    private String contentDigest;

    @Column(nullable = false)
    private String status;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
    @Column(name = "supplied_digest")
    private String suppliedDigest;

    @Column(columnDefinition = "TEXT")
    private String payload;
}
