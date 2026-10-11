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
        name = "uk_scope_display_id", columnNames = {"scope_id"}))
public class ScopeDisplayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scope_key")
    private String scopeKey;

    @Column(name = "scope_id", nullable = false, unique = true)
    private String scopeId;

    @Column(nullable = false)
    private String title;

    @Column()
    private String subtitle;

    @Column(name = "source_revision")
    private Long sourceRevision;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}
