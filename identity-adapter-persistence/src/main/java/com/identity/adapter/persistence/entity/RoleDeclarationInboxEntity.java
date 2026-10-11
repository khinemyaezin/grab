package com.identity.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "role_declaration_inbox")
public class RoleDeclarationInboxEntity {
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;
    @Column(nullable = false, updatable = false)
    private String owner;
    @Column(name = "role_code", nullable = false, updatable = false)
    private String roleCode;
    @Column(nullable = false, updatable = false)
    private int revision;
    @Column(name = "content_digest", length = 64, nullable = false, updatable = false)
    private String contentDigest;
    @Column(length = 40, nullable = false)
    private String status;
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;
}
