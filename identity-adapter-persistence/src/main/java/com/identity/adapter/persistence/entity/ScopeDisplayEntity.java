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

@Getter
@Setter
@Entity
@Table(name = "scope_displays", uniqueConstraints = @UniqueConstraint(
        name = "uk_scope_display_key_id", columnNames = {"scope_key", "scope_id"}))
public class ScopeDisplayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scope_key", nullable = false, length = 255)
    private String scopeKey;

    @Column(name = "scope_id", nullable = false, length = 255)
    private String scopeId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(name = "source_revision", nullable = false)
    private long sourceRevision;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}
