package com.identity.adapter.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "security_manifest_module")
public class SecurityManifestModuleEntity {
    @Id
    @Column(name = "module_key", nullable = false, updatable = false)
    private String moduleKey;

    @Column(name = "applied_revision", nullable = false)
    private int appliedRevision;

    @Column(name = "applied_digest")
    private String appliedDigest;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}
