package com.identity.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role_declaration_state")
public class RoleDeclarationStateEntity {
    @Id
    @Column(name = "role_code", nullable = false, updatable = false)
    private String roleCode;
    @Column(nullable = false, updatable = false)
    private String owner;
    @Column(name = "assignment_scope_key", nullable = false)
    private String assignmentScopeKey;
    @Column(name = "applied_revision", nullable = false)
    private int appliedRevision;
    @Column(name = "applied_digest", length = 64, nullable = false)
    private String appliedDigest = "";
    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}
