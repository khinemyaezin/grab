package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "security_scope_definitions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_security_scope_module_key", columnNames = {"module_key", "scope_key"})
})
public class ScopeManifestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, updatable = false)
    private String moduleKey;

    @Column(name = "scope_key", nullable = false, updatable = false)
    private String scopeKey;

    @Column(name = "parent_scope_key")
    private String parentScopeKey;

    @Column(name = "manifest_version", nullable = false)
    private int manifestVersion;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;
}
