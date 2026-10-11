package com.identity.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "role_declaration_conflict",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_declaration_conflict_event_digest",
                columnNames = {"event_id", "supplied_digest"}))
public class RoleDeclarationConflictEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;
    @Column(nullable = false, updatable = false)
    private String owner;
    @Column(name = "role_code", nullable = false, updatable = false)
    private String roleCode;
    @Column(nullable = false, updatable = false)
    private int revision;
    @Column(name = "supplied_digest", length = 64, nullable = false, updatable = false)
    private String suppliedDigest;
    @Column(name = "existing_digest", length = 64, updatable = false)
    private String existingDigest;
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String reason;
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;
}
