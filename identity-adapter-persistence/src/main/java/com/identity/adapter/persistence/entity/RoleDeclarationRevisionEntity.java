package com.identity.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "role_declaration_revision",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_declaration_revision",
                        columnNames = {"owner", "role_code", "revision"})
        })
public class RoleDeclarationRevisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private String owner;
    @Column(name = "role_code", nullable = false, updatable = false)
    private String roleCode;
    @Column(nullable = false, updatable = false)
    private int revision;
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;
    @Column(name = "content_digest", length = 64, nullable = false, updatable = false)
    private String contentDigest;
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(length = 40, nullable = false)
    private String status;
    @Column(columnDefinition = "TEXT")
    private String reason;
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;
    @Column(name = "applied_at")
    private Instant appliedAt;
    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}
